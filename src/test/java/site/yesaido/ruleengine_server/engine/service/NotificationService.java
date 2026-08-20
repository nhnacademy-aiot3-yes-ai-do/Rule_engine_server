package site.yesaido.ruleengine_server.engine.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import site.yesaido.ruleengine_server.collector.dto.SensorDataDto;
import site.yesaido.ruleengine_server.engine.dto.ThresholdStatus;
import site.yesaido.ruleengine_server.engine.dto.ThresholdStatusChangedEvent;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    private static final String EXCHANGE = "yes-nhn.notification.exchange";
    private static final String ROUTING_KEY = "yes-nhn.notification.threshold.queue";

    @Mock
    private RabbitTemplate rabbitTemplate;

    private NotificationService notificationService;

    private SensorDataDto sensorData;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(rabbitTemplate, EXCHANGE, ROUTING_KEY);

        sensorData = new SensorDataDto(
                "실습실", "후면 오른쪽",
                "AM107", "AM107-067999", "24e124128c067999",
                "TEMPERATURE",
                BigDecimal.valueOf(23.7),
                OffsetDateTime.now(),
                "°C",
                10L
        );
    }

    @Test
    void test_setUpMandatoryReturn_setsMandatoryTrueAndReturnsCallback() {
        notificationService.setUpMandatoryReturn();

        verify(rabbitTemplate, times(1)).setMandatory(true);
        verify(rabbitTemplate, times(1)).setReturnsCallback(any());
    }

    @Test
    void test_sendThresholdExceededAlert_publishesEventWithExceededStatus() {
        notificationService.sendThresholdExceededAlert(sensorData);

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(rabbitTemplate, times(1)).convertAndSend(eq(EXCHANGE), eq(ROUTING_KEY), captor.capture());

        ThresholdStatusChangedEvent event = (ThresholdStatusChangedEvent) captor.getValue();
        org.junit.jupiter.api.Assertions.assertEquals(ThresholdStatus.EXCEEDED, event.getStatus());
        org.junit.jupiter.api.Assertions.assertEquals(sensorData, event.getSensorData());
        org.junit.jupiter.api.Assertions.assertNotNull(event.getEventId());
        org.junit.jupiter.api.Assertions.assertNotNull(event.getOccurredAt());
    }

    @Test
    void test_sendThresholdRecoveredAlert_publishesEventWithRecoveredStatus() {
        notificationService.sendThresholdRecoveredAlert(sensorData);

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(rabbitTemplate, times(1)).convertAndSend(eq(EXCHANGE), eq(ROUTING_KEY), captor.capture());

        ThresholdStatusChangedEvent event = (ThresholdStatusChangedEvent) captor.getValue();
        org.junit.jupiter.api.Assertions.assertEquals(ThresholdStatus.RECOVERED, event.getStatus());
        org.junit.jupiter.api.Assertions.assertEquals(sensorData, event.getSensorData());
    }

    @Test
    void test_sendThresholdExceededAlert_eachCallGeneratesDifferentEventId() {
        notificationService.sendThresholdExceededAlert(sensorData);
        notificationService.sendThresholdExceededAlert(sensorData);

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(rabbitTemplate, times(2)).convertAndSend(eq(EXCHANGE), eq(ROUTING_KEY), captor.capture());

        ThresholdStatusChangedEvent first = (ThresholdStatusChangedEvent) captor.getAllValues().get(0);
        ThresholdStatusChangedEvent second = (ThresholdStatusChangedEvent) captor.getAllValues().get(1);
        org.junit.jupiter.api.Assertions.assertNotEquals(first.getEventId(), second.getEventId());
    }
}