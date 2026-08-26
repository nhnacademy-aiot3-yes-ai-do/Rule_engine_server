package site.yesaido.ruleengine_server.engine.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import site.yesaido.ruleengine_server.collector.dto.SensorDataDto;
import site.yesaido.ruleengine_server.engine.dto.AutomationStateChangedEvent;
import site.yesaido.ruleengine_server.engine.dto.ThresholdStatus;
import site.yesaido.ruleengine_server.engine.dto.ThresholdStatusChangedEvent;
import site.yesaido.ruleengine_server.engine.dto.actuator.ActuatorCommandResponse;
import site.yesaido.ruleengine_server.engine.dto.actuator.ActuatorCommandStatus;
import site.yesaido.ruleengine_server.engine.dto.actuator.ActuatorState;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Slf4j
@Service
public class NotificationService {

    private final String exchange;
    private final String thresholdRoutingKey;
    private final String actionRoutingKey;

    private final RabbitTemplate rabbitTemplate;

    public NotificationService(RabbitTemplate rabbitTemplate,
                               @Value("${custom-rabbitmq.exchange.to-notification}") String exchange,
                               @Value("${custom-rabbitmq.queue.notification-threshold}") String thresholdRoutingKey,
                               @Value("${custom-rabbitmq.queue.notification-action}") String actionRoutingKey) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
        this.thresholdRoutingKey = thresholdRoutingKey;
        this.actionRoutingKey = actionRoutingKey;
    }

    @PostConstruct
    void setUpMandatoryReturn() {
        rabbitTemplate.setMandatory(true);
        rabbitTemplate.setReturnsCallback(returned ->
                log.warn("[NotificationService] 라우팅 실패 - 메시지 버려짐: exchange={}, routingKey={}, replyCode={}, replyText={}",
                        returned.getExchange(), returned.getRoutingKey(), returned.getReplyCode(), returned.getReplyText())
        );
    }

    public void sendThresholdExceededAlert(SensorDataDto sensorData) {
        publishThresholdAlert(sensorData, ThresholdStatus.EXCEEDED);
    }

    public void sendThresholdRecoveredAlert(SensorDataDto sensorData) {
        publishThresholdAlert(sensorData, ThresholdStatus.RECOVERED);
    }

    public void sendActuatorCommandResult(long cultivationId, String actuatorType, ActuatorCommandResponse response, Instant requestedAt) {

        AutomationStateChangedEvent event = new AutomationStateChangedEvent(
                response.controlId(),
                cultivationId,
                actuatorType,
                response.status().getMessage(),
                response.status() == ActuatorCommandStatus.APPLIED,
                requestedAt.atOffset(ZoneOffset.ofHours(9))
        );

        publishActionAlert(event);
    }

    private void publishThresholdAlert(SensorDataDto sensorData, ThresholdStatus status) {

        ThresholdStatusChangedEvent event = new ThresholdStatusChangedEvent(
                UUID.randomUUID(),
                sensorData,
                status,
                OffsetDateTime.now(ZoneOffset.UTC).withOffsetSameInstant(ZoneOffset.ofHours(9))
        );

        rabbitTemplate.convertAndSend(exchange, thresholdRoutingKey, event);
        log.debug("[NotificationService] 임계값 알림 발행: status={}, deviceEui={}, sensorType={}",
                status, sensorData.getDeviceEui(), sensorData.getSensorType());
    }

    private void publishActionAlert(AutomationStateChangedEvent event) {

        rabbitTemplate.convertAndSend(exchange, actionRoutingKey, event);
        log.debug("[NotificationService] 액추에이터 알림 발행: cultivationId={}, actuatorType={}",
                event.getCultivationId(), event.getActuatorType());
    }

}
