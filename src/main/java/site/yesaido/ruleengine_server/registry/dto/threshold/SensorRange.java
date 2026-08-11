package site.yesaido.ruleengine_server.registry.dto.threshold;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import site.yesaido.ruleengine_server.global.dto.SensorType;

import java.math.BigDecimal;

/**
 * 개별 센서 타입별 임계값 유효 범위 및 단위 정보를 담는 DTO입니다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SensorRange {

    @NotNull
    private SensorType sensorType;

    @NotNull
    private String unit;

    @NotNull
    private BigDecimal minValue;
    @NotNull
    private BigDecimal maxValue;

}
