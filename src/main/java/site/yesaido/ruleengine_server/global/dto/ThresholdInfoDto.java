package site.yesaido.ruleengine_server.global.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import site.yesaido.ruleengine_server.registry.dto.threshold.SensorRange;

import java.util.List;

/**
 * 재배 환경 정보를 담고 있는 DTO입니다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ThresholdInfoDto {

    @NotNull
    private Long cultivationId;

    // 온도 최솟값, 최댓값
    @NotNull
    private Double tempMin;
    @NotNull
    private Double tempMax;

    // 습도
    @NotNull
    private Double humidityMin;
    @NotNull
    private Double humidityMax;

    // CO2
    @NotNull
    private Double co2Min;
    @NotNull
    private Double co2Max;

    // 조도
    @NotNull
    private Double lightMin;
    @NotNull
    private Double lightMax;

    public static ThresholdInfoDto from(Long cultivationId, List<SensorRange> sensorRangeList) {
        ThresholdInfoDto dto = new ThresholdInfoDto();

        dto.setCultivationId(cultivationId);
        sensorRangeList.forEach(dto::applyRange);

        return dto;
    }

    public void applyRange(SensorRange sensorRange) {

        Double minValue = sensorRange.getMinValue() != null ? sensorRange.getMinValue().doubleValue() : null;
        Double maxValue = sensorRange.getMaxValue() != null ? sensorRange.getMaxValue().doubleValue() : null;

        switch (sensorRange.getSensorType()) {
            case TEMPERATURE -> {
                this.tempMin = minValue;
                this.tempMax = maxValue;
            }
            case HUMIDITY -> {
                this.humidityMin = minValue;
                this.humidityMax = maxValue;
            }
            case CO2 -> {
                this.co2Min = minValue;
                this.co2Max = maxValue;
            }
            case LIGHT -> {
                this.lightMin = minValue;
                this.lightMax = maxValue;
            }
        }
    }

}
