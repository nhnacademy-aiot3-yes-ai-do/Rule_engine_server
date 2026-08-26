package site.yesaido.ruleengine_server.engine.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import site.yesaido.ruleengine_server.collector.dto.SensorDataDto;
import site.yesaido.ruleengine_server.engine.dto.AutomationStateChangedEvent;
import site.yesaido.ruleengine_server.engine.dto.ThresholdStatus;
import site.yesaido.ruleengine_server.engine.dto.ThresholdStatusChangedEvent;
import site.yesaido.ruleengine_server.engine.dto.actuator.ActuatorCommandResponse;
import site.yesaido.ruleengine_server.engine.dto.actuator.ActuatorCommandStatus;
import site.yesaido.ruleengine_server.engine.dto.actuator.ActuatorState;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    private static final String EXCHANGE = "yes-nhn.notification.exchange";
    private static final String THRESHOLD_ROUTING_KEY = "yes-nhn.notification.threshold.queue";
    private static final String ACTION_ROUTING_KEY = "yes-nhn.notification.threshold.queue";

    @Mock
    private RabbitTemplate rabbitTemplate;

    private NotificationService notificationService;

    private SensorDataDto sensorData;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(rabbitTemplate, EXCHANGE, THRESHOLD_ROUTING_KEY, ACTION_ROUTING_KEY);

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
        verify(rabbitTemplate, times(1)).convertAndSend(eq(EXCHANGE), eq(THRESHOLD_ROUTING_KEY), captor.capture());

        ThresholdStatusChangedEvent event = (ThresholdStatusChangedEvent) captor.getValue();
        Assertions.assertEquals(ThresholdStatus.EXCEEDED, event.getStatus());
        Assertions.assertEquals(sensorData, event.getSensorData());
        Assertions.assertNotNull(event.getEventId());
        Assertions.assertNotNull(event.getOccurredAt());
    }

    @Test
    void test_sendThresholdRecoveredAlert_publishesEventWithRecoveredStatus() {
        notificationService.sendThresholdRecoveredAlert(sensorData);

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(rabbitTemplate, times(1)).convertAndSend(eq(EXCHANGE), eq(THRESHOLD_ROUTING_KEY), captor.capture());

        ThresholdStatusChangedEvent event = (ThresholdStatusChangedEvent) captor.getValue();
        Assertions.assertEquals(ThresholdStatus.RECOVERED, event.getStatus());
        Assertions.assertEquals(sensorData, event.getSensorData());
    }

    @Test
    void test_sendThresholdExceededAlert_eachCallGeneratesDifferentEventId() {
        notificationService.sendThresholdExceededAlert(sensorData);
        notificationService.sendThresholdExceededAlert(sensorData);

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(rabbitTemplate, times(2)).convertAndSend(eq(EXCHANGE), eq(THRESHOLD_ROUTING_KEY), captor.capture());

        ThresholdStatusChangedEvent first = (ThresholdStatusChangedEvent) captor.getAllValues().get(0);
        ThresholdStatusChangedEvent second = (ThresholdStatusChangedEvent) captor.getAllValues().get(1);
        Assertions.assertNotEquals(first.getEventId(), second.getEventId());
    }

    @Test
    void test_sendActuatorCommandResult_whenApplied_isOnTrue() {
        UUID controlId = UUID.randomUUID();
        Instant requestedAt = Instant.parse("2026-08-25T00:00:00Z");
        ActuatorCommandResponse response = new ActuatorCommandResponse(
                controlId,
                UUID.randomUUID(),
                ActuatorCommandStatus.APPLIED,
                ActuatorState.ON,
                requestedAt
        );

        notificationService.sendActuatorCommandResult(10L, "HEATER", response, requestedAt);

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(rabbitTemplate, times(1)).convertAndSend(eq(EXCHANGE), eq(ACTION_ROUTING_KEY), captor.capture());

        AutomationStateChangedEvent event = (AutomationStateChangedEvent) captor.getValue();
        Assertions.assertEquals(controlId, event.getEventId());
        Assertions.assertEquals(10L, event.getCultivationId());
        Assertions.assertEquals("HEATER", event.getActuatorType());
        Assertions.assertEquals(ActuatorCommandStatus.APPLIED.getMessage(), event.getMessage());
        Assertions.assertTrue(event.isEnabled());
        Assertions.assertEquals(requestedAt.atOffset(ZoneOffset.ofHours(9)), event.getOccurredAt());
    }

    @Test
    void test_sendActuatorCommandResult_whenRejected_isOnFalseRegardlessOfActualState() {
        Instant requestedAt = Instant.now();
        ActuatorCommandResponse response = new ActuatorCommandResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                ActuatorCommandStatus.REJECTED_CONFLICT,
                ActuatorState.ON,
                requestedAt
        );

        notificationService.sendActuatorCommandResult(10L, "HEATER", response, requestedAt);

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(rabbitTemplate, times(1)).convertAndSend(eq(EXCHANGE), eq(ACTION_ROUTING_KEY), captor.capture());

        AutomationStateChangedEvent event = (AutomationStateChangedEvent) captor.getValue();
        Assertions.assertFalse(event.isEnabled());
        Assertions.assertEquals(ActuatorCommandStatus.REJECTED_CONFLICT.getMessage(), event.getMessage());
    }
}