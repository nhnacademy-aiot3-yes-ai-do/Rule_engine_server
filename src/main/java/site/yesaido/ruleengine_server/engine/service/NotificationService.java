package site.yesaido.ruleengine_server.engine.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import site.yesaido.ruleengine_server.collector.dto.SensorDataDto;
import site.yesaido.ruleengine_server.engine.dto.ThresholdStatus;
import site.yesaido.ruleengine_server.engine.dto.ThresholdStatusChangedEvent;
import site.yesaido.ruleengine_server.engine.dto.actuator.ActuatorCommandStatus;
import site.yesaido.ruleengine_server.engine.dto.actuator.ActuatorControlKey;
import site.yesaido.ruleengine_server.engine.dto.actuator.ActuatorState;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Slf4j
@Service
public class NotificationService {

    private final String exchange;
    private final String routingKey;

    private final RabbitTemplate rabbitTemplate;

    public NotificationService(RabbitTemplate rabbitTemplate,
                               @Value("${custom-rabbitmq.exchange.to-notification}") String exchange,
                               @Value("${custom-rabbitmq.queue.notification-threshold}") String routingKey) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
        this.routingKey = routingKey;
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
        publish(sensorData, ThresholdStatus.EXCEEDED);
    }

    public void sendThresholdRecoveredAlert(SensorDataDto sensorData) {
        publish(sensorData, ThresholdStatus.RECOVERED);
    }

    public void sendActuatorCommandResult(ActuatorControlKey key, String actuatorType, ActuatorState desiredState, ActuatorCommandStatus status) {

        log.debug("[NotificationService] key={}, actuatorType={}, actuatorState={}, actuatorCommandStaus={}", key, actuatorType, desiredState.name(), status.name());
        // 다음 해야할 일 : NotificationService로의 액추에이터 제어 명령에 대한 RabbitMQ 발행
    }

    private void publish(SensorDataDto sensorData, ThresholdStatus status) {
        ThresholdStatusChangedEvent event = new ThresholdStatusChangedEvent(
                UUID.randomUUID(),
                sensorData,
                status,
                OffsetDateTime.now(ZoneOffset.UTC).withOffsetSameInstant(ZoneOffset.ofHours(9))
        );

        log.debug("알림 발행 테스트: {}", event);
        rabbitTemplate.convertAndSend(exchange, routingKey, event);
        log.debug("[NotificationService] 알림 이벤트 발행: status={}, deviceEui={}, sensorType={}",
                status, sensorData.getDeviceEui(), sensorData.getSensorType());
    }

}
