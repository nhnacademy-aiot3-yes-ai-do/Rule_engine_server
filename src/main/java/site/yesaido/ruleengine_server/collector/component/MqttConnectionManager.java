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

    @Override
    public void start() {
        log.info(">>> MqttConnectionManager.start() 호출됨");
        try {
            mqttAsyncClient.setCallback(mqttMessageSubscriber);
            mqttAsyncClient.connect(mqttConnectionOptions).waitForCompletion();

            log.info("등록된 토픽: {}", Arrays.toString(topics));
            MqttSubscription[] mqttSubscriptions = Arrays.stream(topics)
                            .map(topic -> topic.trim())
                            .map(topic -> new MqttSubscription(topic, 0))
                            .toArray(size -> new MqttSubscription[size]);

            mqttAsyncClient.subscribe(mqttSubscriptions).waitForCompletion();

            running = true;
            log.info("MQTT 연결 및 구독 완료 - topics={}", (Object) mqttSubscriptions);
        } catch (Exception e) {
            throw new IllegalStateException("MQTT 연결 실패: ", e);
        }
    }

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
