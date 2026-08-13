package site.yesaido.ruleengine_server.registry.component;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import site.yesaido.ruleengine_server.registry.dto.threshold.ThresholdInfoEvent;
import site.yesaido.ruleengine_server.registry.service.ThresholdInfoService;

/**
 * RabbitMQ 큐로부터 임계치 정보 이벤트({@link ThresholdInfoEvent})를 수신(Consume)하는 클래스입니다.
 * <p>
 * 수신된 메시지를 검증한 후 {@link ThresholdInfoService}로 전달하여
 * 재배 환경별 센서 임계치 정보의 등록, 수정, 삭제 등의 비즈니스 로직을 처리하도록 요청합니다.
 * </p>
 */
@RequiredArgsConstructor
@Component
public class ThresholdInfoConsumer {

    private final ThresholdInfoService thresholdInfoService;

    /**
     * RabbitMQ 큐에서 임계치 정보 이벤트를 수신하여 처리합니다.
     *
     * @param thresholdInfoEvent 수신된 임계치 정보 이벤트 DTO
     */
    @RabbitListener(queues = "${custom-rabbitmq.queue.threshold-info}", errorHandler = "validationErrorHandler")
    public void consumeThresholdInfoEvent(@Payload @Valid ThresholdInfoEvent thresholdInfoEvent) {

        thresholdInfoService.processThresholdInfoEvent(thresholdInfoEvent);
    }

}
