package site.yesaido.ruleengine_server.registry.component;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import site.yesaido.ruleengine_server.registry.dto.sensor.SensorInfoDeleteEvent;
import site.yesaido.ruleengine_server.global.dto.SensorInfoDto;
import site.yesaido.ruleengine_server.registry.service.SensorInfoService;

@RequiredArgsConstructor
@RabbitListener(queues = "${custom-rabbitmq.queue.sensor-info}", errorHandler = "validationErrorHandler")
@Component
public class SensorInfoConsumer {

    private final SensorInfoService sensorInfoService;

    @RabbitHandler
    public void consumeSensorInfoUpsert(@Payload @Valid SensorInfoDto sensorInfoDto) {

        sensorInfoService.upsertSensorInfo(sensorInfoDto);
    }

    @RabbitHandler
    public void consumeSensorInfoDelete(@Payload @Valid SensorInfoDeleteEvent sensorInfoDeleteEvent) {

        sensorInfoService.deleteSensorInfo(sensorInfoDeleteEvent);
    }
}
