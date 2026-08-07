package site.yesaido.ruleengine_server.registry.component;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import site.yesaido.ruleengine_server.registry.dto.threshold.ThresholdInfoDeleteEvent;
import site.yesaido.ruleengine_server.registry.dto.threshold.ThresholdInfoEvent;
import site.yesaido.ruleengine_server.registry.service.CultivationInfoService;

@RequiredArgsConstructor
@RabbitListener(queues = "${custom-rabbitmq.queue.cultivation-info}", errorHandler = "validationErrorHandler")
@Component
public class CultivationInfoConsumer {

    private final CultivationInfoService cultivationInfoService;

    @RabbitHandler
    public void consumeCultivationInfoUpsert(@Payload @Valid ThresholdInfoEvent thresholdInfoEvent) {

        cultivationInfoService.upsertCultivationInfo(thresholdInfoEvent);
    }

    @RabbitHandler
    public void consumeCultivationInfoDelete(@Payload @Valid ThresholdInfoDeleteEvent thresholdInfoDeleteEvent) {

        cultivationInfoService.deleteCultivationInfo(thresholdInfoDeleteEvent);
    }
}
