package site.yesaido.ruleengine_server.registry.dto.sensor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import site.yesaido.ruleengine_server.global.dto.SensorType;

/**
 * 센서 정보를 담고 있는 DTO입니다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SensorInfoUpsertEvent {

    @NotNull
    private Long cultivationId;

    @NotBlank
    private String location;

    @NotBlank
    private String locationDetail;

    @NotBlank
    private String deviceModel;

    @NotBlank
    private String deviceName;

    @NotBlank
    private String deviceEui;

    @NotNull
    private SensorType sensorType;

    @NotNull
    private String unit;
}