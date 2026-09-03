package site.yesaido.ruleengine_server.registry.dto.sensor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

/**
 * 센서 정보 삭제를 위한 이벤트 DTO입니다.
 */
@Getter
@Setter
@NoArgsConstructor
public class SensorInfoDeleteEvent {

    @NotNull
    private Long cultivationId;

    @NotBlank
    private String deviceEui;

    @NotBlank
    private String sensorType;

    @NotBlank
    private String unit;

    @NotNull
    OffsetDateTime occurredAt;
}