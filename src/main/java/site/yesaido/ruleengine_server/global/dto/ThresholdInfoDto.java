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
 * 재배 환경별 센서 임계값 정보를 담고 있는 DTO입니다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ThresholdInfoDto {

    /**
     * 재배 환경 식별자(ID)
     */
    @NotNull
    private Long cultivationId;

    /**
     * 센서 타입 및 단위별 적정 범위 매핑 맵 (Key: "{sensorType}_{unit}")
     */
    private Map<String, SensorRange> ranges = new HashMap<>();

    /**
     * 재배 환경 ID와 센서 임계값 범위 목록으로부터 {@link ThresholdInfoDto}를 생성하는 정적 팩토리 메서드입니다.
     *
     * @param cultivationId   재배 환경 ID
     * @param sensorRangeList 센서 임계값 범위 목록
     * @return 매핑된 ThresholdInfoDto 인스턴스
     */
    public static ThresholdInfoDto from(Long cultivationId, List<SensorRange> sensorRangeList) {

        ThresholdInfoDto dto = new ThresholdInfoDto();

        dto.setCultivationId(cultivationId);
        sensorRangeList.forEach(dto::applyRange);

        return dto;
    }

    /**
     * 개별 {@link SensorRange} 정보를 기반으로 해당 센서 타입의 임계 범위를 추가하거나 갱신합니다.
     *
     * @param sensorRange 적용할 센서 범위 정보
     */
    public void applyRange(SensorRange sensorRange) {

        ranges.put(
                buildKey(sensorRange.getSensorType(), sensorRange.getUnit()),
                sensorRange
        );
    }

    /**
     * 특정 키(예: "{sensorType}_{unit}")에 해당하는 임계값 범위를 조회합니다.
     *
     * @param sensorType 조회할 키로 사용할 센서 타입
     * @param unit 조회할 키로 사용할 단위
     * @return 해당 센서의 임계값 범위({@link SensorRange})
     */
    public SensorRange getRange(String sensorType, String unit) {
        return ranges.get(
                buildKey(sensorType, unit)
        );
    }

    // ======================================================================

    /**
     * 센서 타입과 단위를 조합하여 Map의 고유 키를 생성합니다.
     *
     * @param sensorType 센서 종류
     * @param unit       측정 단위
     * @return "{sensorType}_{unit}" 형식의 복합 키 문자열
     */
    private String buildKey(String sensorType, String unit) {
        return "%s_%s".formatted(sensorType, unit);
    }
}
