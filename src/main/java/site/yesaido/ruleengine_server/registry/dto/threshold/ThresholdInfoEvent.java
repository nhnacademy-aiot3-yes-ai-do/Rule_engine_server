package site.yesaido.ruleengine_server.registry.dto.threshold;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * 재배 환경(Cultivation)의 임계값 설정 변경 시 발행되는 이벤트 DTO입니다.<br>
 * 센서 범위 목록({@link SensorRange})의 크기(Size)에 따라 이벤트 유형이 구분됩니다.
 * <ul>
 *     <li><b>추가 (Create)</b> : 센서 4종의 임계값을 한 번에 설정 (List size = 4)</li>
 *     <li><b>수정 (Update)</b> : 특정 센서 1종의 임계값 변경 (List size = 1)</li>
 *     <li><b>삭제 (Delete)</b> : 임계값 정보 전체 삭제 (List size = 0, 빈 리스트)</li>
 * </ul>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ThresholdInfoEvent {

    @NotNull
    private Long cultivationId;

    @NotNull
    private List<SensorRange> sensorRangeList;

}
/*
    재배 환경 정보 추가/수정/삭제

    - 추가 : 원소가 4개인 List<SensorRange>
    - 수정 : 원소가 1개인 List<SensorRange>
    - 삭제 : 원소가 0개인 List.of()
 */
