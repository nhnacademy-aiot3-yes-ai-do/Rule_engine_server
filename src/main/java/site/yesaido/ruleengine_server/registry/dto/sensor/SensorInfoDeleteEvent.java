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

    /**
     * 센서가 속한 재배 환경의 식별자(ID)
     */
    @NotNull
    private Long cultivationId;

    /**
     * 센서 디바이스의 EUI (고유 식별자)
     */
    @NotBlank
    private String deviceEui;

    /**
     * 센서 종류 (예: TEMPERATURE, HUMIDITY 등)
     */
    @NotBlank
    private String sensorType;

    /**
     * 센서 측정 단위 (예: °C, %, ppm 등)
     */
    @NotBlank
    private String unit;

    /**
     * 이벤트 발생 시각
     */
    @NotNull
    OffsetDateTime occurredAt;
}