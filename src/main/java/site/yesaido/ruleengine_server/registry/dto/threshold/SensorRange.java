package site.yesaido.ruleengine_server.registry.dto.threshold;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    /**
     * 센서 종류 정규화 및 측정 범위 설정을 위한 생성자입니다.
     *
     * @param sensorType 센서 종류
     * @param unit       측정 단위
     * @param minValue   임계값 최솟값
     * @param maxValue   임계값 최댓값
     */
    public SensorRange(String sensorType, String unit, BigDecimal minValue, BigDecimal maxValue) {
        this.sensorType = SensorTypeUtils.normalize(sensorType);
        this.unit = unit;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }
}
