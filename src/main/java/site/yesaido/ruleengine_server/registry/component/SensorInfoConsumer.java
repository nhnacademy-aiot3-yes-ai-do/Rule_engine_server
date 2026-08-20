package site.yesaido.ruleengine_server.registry.component;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import site.yesaido.ruleengine_server.registry.dto.sensor.SensorInfoDeleteEvent;
import site.yesaido.ruleengine_server.registry.dto.sensor.SensorInfoUpsertEvent;
import site.yesaido.ruleengine_server.registry.service.SensorInfoService;

/**
 * RabbitMQ 큐로부터 센서 정보 이벤트({@link SensorInfoUpsertEvent}, {@link SensorInfoDeleteEvent})를 수신(Consume)하는 클래스입니다.
 * <p>
 * 수신된 메시지를 검증한 후 {@link SensorInfoService}로 전달하여
 * 센서 정보의 등록, 수정, 삭제 등의 비즈니스 로직을 처리하도록 요청합니다.
 * </p>
 */
@RequiredArgsConstructor
@RabbitListener(queues = "${custom-rabbitmq.queue.sensor-info}", errorHandler = "validationErrorHandler")
@Component
public class SensorInfoConsumer {

    private final SensorInfoService sensorInfoService;

    /**
     * RabbitMQ 큐에서 센서 정보 생성 및 갱신 이벤트를 수신하여 처리합니다.
     *
     * @param sensorInfoUpsertEvent 수신된 센서 정보 생성 및 갱신 이벤트 DTO
     */
    @RabbitHandler
    public void consumeSensorInfoUpsertEvent(@Payload @Valid SensorInfoUpsertEvent sensorInfoUpsertEvent) {

        sensorInfoService.upsertSensorInfo(sensorInfoUpsertEvent);
    }

    /**
     * RabbitMQ 큐에서 센서 정보 삭제 이벤트를 수신하여 처리합니다.
     *
     * @param sensorInfoDeleteEvent 수신된 센서 정보 삭제 이벤트 DTO
     */
    @RabbitHandler
    public void consumeSensorInfoDeleteEvent(@Payload @Valid SensorInfoDeleteEvent sensorInfoDeleteEvent) {

        sensorInfoService.deleteSensorInfo(sensorInfoDeleteEvent);
    }
}
