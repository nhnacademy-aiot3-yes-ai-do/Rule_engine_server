package site.yesaido.ruleengine_server.registry.component;

import org.eclipse.paho.mqttv5.client.IMqttToken;
import org.eclipse.paho.mqttv5.client.MqttDisconnectResponse;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.yesaido.ruleengine_server.collector.component.MqttMessageSubscriber;
import site.yesaido.ruleengine_server.collector.service.CollectorService;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MqttMessageSubscriberTest {

    @Mock
    private CollectorService collectorService;

    private MqttMessageSubscriber mqttMessageSubscriber;

    @BeforeEach
    void setUp() {
        // 비동기 검증을 피하기 위해 실제 스레드풀 대신 동기 실행 Executor를 직접 주입
        mqttMessageSubscriber = new MqttMessageSubscriber(collectorService, Runnable::run);
    }

    @Test
    @DisplayName("메세지 수신 시 CollectorService.ingest에 topic/payload를 그대로 전달한다")
    void test_messageArrived_delegatesToCollectorService() throws Exception {
        MqttMessage mqttMessage = new MqttMessage("hello".getBytes(StandardCharsets.UTF_8));

        mqttMessageSubscriber.messageArrived("mushroom/x", mqttMessage);

        verify(collectorService, times(1)).ingest("mushroom/x", "hello");
    }

    @Test
    @DisplayName("CollectorService.ingest에서 예외가 발생해도 밖으로 전파되지 않는다")
    void test_messageArrived_exceptionInIngest_isSwallowed() {
        doThrow(new RuntimeException("처리 실패")).when(collectorService).ingest(anyString(), anyString());
        MqttMessage mqttMessage = new MqttMessage("bad".getBytes(StandardCharsets.UTF_8));

        assertDoesNotThrow(() -> mqttMessageSubscriber.messageArrived("mushroom/x", mqttMessage));
    }

    @Test
    @DisplayName("disconnected 콜백은 예외 발생 원인이 있어도 없어도 예외를 던지지 않는다")
    void test_disconnected_withAndWithoutException_doesNotThrow() {
        MqttDisconnectResponse withCause = mock(MqttDisconnectResponse.class);
        when(withCause.getReturnCode()).thenReturn(128);
        when(withCause.getReasonString()).thenReturn("Normal disconnection");
        when(withCause.getException()).thenReturn(new MqttException(new RuntimeException("연결 끊김 원인")));

        MqttDisconnectResponse withoutCause = mock(MqttDisconnectResponse.class);
        when(withoutCause.getReturnCode()).thenReturn(0);
        when(withoutCause.getReasonString()).thenReturn(null);
        when(withoutCause.getException()).thenReturn(null);

        assertDoesNotThrow(() -> mqttMessageSubscriber.disconnected(withCause));
        assertDoesNotThrow(() -> mqttMessageSubscriber.disconnected(withoutCause));
    }

    @Test
    @DisplayName("mqttErrorOccurred 콜백은 예외를 던지지 않는다")
    void test_mqttErrorOccurred_doesNotThrow() {
        assertDoesNotThrow(() ->
                mqttMessageSubscriber.mqttErrorOccurred(new MqttException(new RuntimeException("에러"))));
    }

    @Test
    @DisplayName("deliveryComplete 콜백은 아무 동작 없이 예외를 던지지 않는다")
    void test_deliveryComplete_doesNothing() {
        assertDoesNotThrow(() -> mqttMessageSubscriber.deliveryComplete(mock(IMqttToken.class)));
    }

    @Test
    @DisplayName("connectComplete는 재연결(b=true)/최초연결(b=false) 모두 예외 없이 처리한다")
    void test_connectComplete_bothBranches_doNotThrow() {
        assertDoesNotThrow(() -> mqttMessageSubscriber.connectComplete(true, "tcp://localhost:1883"));
        assertDoesNotThrow(() -> mqttMessageSubscriber.connectComplete(false, "tcp://localhost:1883"));
    }

    @Test
    @DisplayName("authPacketArrived 콜백은 예외를 던지지 않는다")
    void test_authPacketArrived_doesNotThrow() {
        assertDoesNotThrow(() ->
                mqttMessageSubscriber.authPacketArrived(1, mock(MqttProperties.class)));
    }
}