package site.yesaido.ruleengine_server.registry.component;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import site.yesaido.ruleengine_server.registry.dto.threshold.ThresholdInfoEvent;
import site.yesaido.ruleengine_server.registry.service.ThresholdInfoService;

@RequiredArgsConstructor
@Component
public class ThresholdInfoConsumer {

    private final ThresholdInfoService thresholdInfoService;

    @RabbitListener(queues = "${custom-rabbitmq.queue.threshold-info}", errorHandler = "validationErrorHandler")
    public void consumeThresholdInfoEvent(@Payload @Valid ThresholdInfoEvent thresholdInfoEvent) {

        thresholdInfoService.processThresholdInfoEvent(thresholdInfoEvent);
    }

}
