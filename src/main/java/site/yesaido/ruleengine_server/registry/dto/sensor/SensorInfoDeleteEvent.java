package site.yesaido.ruleengine_server.registry.dto.sensor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import site.yesaido.ruleengine_server.global.dto.SensorType;

/**
 * 센서 정보 삭제을 위한 DTO입니다.
 */
@Getter
@Setter
@NoArgsConstructor
public class SensorInfoDeleteEvent {

    @NotNull
    private Long cultivationId;

    @NotNull
    private String deviceEui;

    @NotNull
    private SensorType sensorType;

    @NotBlank
    private String unit;
}