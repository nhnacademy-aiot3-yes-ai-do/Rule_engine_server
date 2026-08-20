package site.yesaido.ruleengine_server.collector.component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.mqttv5.client.IMqttToken;
import org.eclipse.paho.mqttv5.client.MqttCallback;
import org.eclipse.paho.mqttv5.client.MqttDisconnectResponse;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.springframework.stereotype.Component;
import site.yesaido.ruleengine_server.collector.service.CollectorService;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executor;

@Slf4j
@RequiredArgsConstructor
@Component
public class MqttMessageSubscriber implements MqttCallback {

    private final CollectorService collectorService;
    private final Executor mqttIngestExecutor;

    /**
     * MQTT 브로커와 연결이 끊어졌을 때 호출되는 메서드입니다.
     *
     * @param mqttDisconnectResponse 연결 끊김 원인 및 응답 정보를 포함하는 객체
     */
    @Override
    public void disconnected(MqttDisconnectResponse mqttDisconnectResponse) {
        log.warn("MQTT disconnected: {}", mqttDisconnectResponse.getReasonString());
    }

    /**
     * MQTT 클라이언트 동작 중 에러가 발생했을 때 호출되는 메서드입니다.
     *
     * @param e 발생한 MQTT 예외
     */
    @Override
    public void mqttErrorOccurred(MqttException e) {
        log.error("MQTT errorOccurred: ", e);
    }

    /**
     * 구독 중인 토픽으로 새로운 MQTT 메세지가 도착했을 떄 호출되는 메서드입니다.<br>
     * 메인 MQTT 콜백 스레드의 병목을 방지하기 위해, 수신되는 메세지는 {@link Executor} 스레드 풀을 통해 비동기로 처리합니다.
     *
     * @param s 메시지가 수신된 MQTT 토픽
     * @param mqttMessage 수신된 MQTT 메세지 객체
     * @throws Exception 처리 과정 중 발생할 수 있는 예외
     */
    @Override
    public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
        String payload = new String(mqttMessage.getPayload(), StandardCharsets.UTF_8);

        mqttIngestExecutor.execute(() -> {
            try {
                collectorService.ingest(s, payload);
            } catch (Exception e) {
                log.warn("MQTT 메세지 처리 실패 : topic={}, payload={}", s, payload, e);
            }
        });
    }

    /**
     * 메세지 발행이 완료되었을 때 호출되는 메서드입니다.
     *
     * @param iMqttToken 발행 완료 상태를 확인할 수 있는 토큰
     */
    @Override
    public void deliveryComplete(IMqttToken iMqttToken) {
        // MQTT 구독만 수행하므로, 발행 완료 콜백은 필요하지 않음
    }

    /**
     * MQTT 브로커와의 연결이 성공적으로 완료되었을 때 호출되는 메서드입니다.
     *
     * @param b {@code  true}인 경우 재연결, {@code  false}인 경우 최초 연결을 의미
     * @param s 연결된 MQTT 브로커의 서버 URI
     */
    @Override
    public void connectComplete(boolean b, String s) {
        if (b) {
            log.debug("MQTT connectComplete: serverURI={}", s);
        }
    }

    /**
     * MQTT v5 챌린지-응답 방식의 확장 인증 패킷이 도착했을 때 호출되는 메서드입니다.
     *
     * @param i 인증 단계 코드
     * @param mqttProperties 인증 관련 MQTT 프로퍼티
     */
    @Override
    public void authPacketArrived(int i, MqttProperties mqttProperties) {
        // 확장 인증 미사용
    }
}
