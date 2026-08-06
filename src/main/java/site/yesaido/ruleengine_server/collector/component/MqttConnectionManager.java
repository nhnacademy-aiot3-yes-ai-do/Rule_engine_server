package site.yesaido.ruleengine_server.collector.component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.mqttv5.client.MqttAsyncClient;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttSubscription;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * {@link SmartLifecycle}을 구현하여, 컨테이너가 구동될 때 MQTT 브로커 연결 및 토픽 구독을 수행합니다.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class MqttConnectionManager implements SmartLifecycle {

    private final MqttAsyncClient mqttAsyncClient;
    private final MqttConnectionOptions mqttConnectionOptions;
    private final MqttMessageSubscriber mqttMessageSubscriber;

    @Value("${mqtt.subscribe-topics}")
    private String[] topics;

    private volatile boolean running = false;

    /**
     * Spring 컨테이너가 시작될 때 자동으로 호출되는 메서드입니다.<br>
     * MQTT 메세지 수신을 위한 콜백을 등록하고, 브로커에 연결한 뒤 설정된 토픽들을 구독합니다.
     */
    @Override
    public void start() {
        log.debug(">>> MqttConnectionManager.start() 호출됨");
        try {
            mqttAsyncClient.setCallback(mqttMessageSubscriber);
            mqttAsyncClient.connect(mqttConnectionOptions).waitForCompletion();

            log.debug("등록된 토픽: {}", Arrays.toString(topics));
            MqttSubscription[] mqttSubscriptions = Arrays.stream(topics)
                            .map(String::trim)
                            .map(topic -> new MqttSubscription(topic, 0))
                            .toArray(MqttSubscription[]::new);

            mqttAsyncClient.subscribe(mqttSubscriptions).waitForCompletion();

            running = true;
            log.debug("MQTT 연결 및 구독 완료 - topics={}", (Object) mqttSubscriptions);
        } catch (Exception e) {
            throw new IllegalStateException("MQTT 연결 실패: ", e);
        }
    }

    /**
     * Spring 컴테이너가 종료될 때 자동으로 호출되는 메서드입니다.<br>
     * MQTT 브로커와의 연결을 안전하게 해제하여 자원을 반납합니다.
     */
    @Override
    public void stop() {
        try {
            mqttAsyncClient.disconnect().waitForCompletion();
        } catch (MqttException e) {
            log.warn("MQTT 연결 해제 중 오류: ", e);
        } finally {
            running = false;
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }
}
