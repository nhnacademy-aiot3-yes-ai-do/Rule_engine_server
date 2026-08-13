package site.yesaido.ruleengine_server.registry.dto.threshold;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import site.yesaido.ruleengine_server.global.util.SensorTypeUtils;

import java.math.BigDecimal;

/**
 * 개별 센서 타입별 임계값 유효 범위 및 단위 정보를 담는 DTO입니다.
 */
@Getter
@Setter
@NoArgsConstructor
public class SensorRange {

    @NotBlank
    private String sensorType;

    @NotBlank
    private String unit;

    @NotNull
    private BigDecimal minValue;
    @NotNull
    private BigDecimal maxValue;

    public SensorRange(String sensorType, String unit, BigDecimal minValue, BigDecimal maxValue) {
        this.sensorType = SensorTypeUtils.normalize(sensorType);
        this.unit = unit;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }
}
