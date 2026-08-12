package site.yesaido.ruleengine_server.global.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import site.yesaido.ruleengine_server.registry.dto.threshold.SensorRange;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private Map<String, Range> ranges = new HashMap<>();

    public static ThresholdInfoDto from(Long cultivationId, List<SensorRange> sensorRangeList) {

        ThresholdInfoDto dto = new ThresholdInfoDto();

        dto.setCultivationId(cultivationId);
        sensorRangeList.forEach(dto::applyRange);

        return dto;
    }

    public void applyRange(SensorRange sensorRange) {

        ranges.put(
                sensorRange.getSensorType(),
                new Range(sensorRange.getMinValue(), sensorRange.getMaxValue())
        );
    }

    public Range getRange(String sensorType) {
        return ranges.get(sensorType);
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Range {

        private BigDecimal min;
        private BigDecimal max;
    }
}
