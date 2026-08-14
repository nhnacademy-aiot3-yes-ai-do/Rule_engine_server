package site.yesaido.ruleengine_server.registry.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import site.yesaido.ruleengine_server.global.dto.ThresholdInfoDto;
import site.yesaido.ruleengine_server.registry.exception.ThresholdInfoNotFoundException;
import site.yesaido.ruleengine_server.registry.dto.threshold.SensorRange;
import site.yesaido.ruleengine_server.registry.dto.threshold.ThresholdInfoEvent;
import site.yesaido.ruleengine_server.registry.repository.ThresholdInfoRepository;

import java.util.List;
import java.util.Optional;

/**
 * 재배 환경 정보(= 임계값) 관리를 담당하는 서비스 클래스입니다.<br>
 * 수신된 이벤트에 따라 재배 환경 임계값의 등록, 수정, 삭제 및 센서 타입 동적 관리를 처리합니다.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ThresholdInfoService {

    private final ThresholdInfoRepository thresholdInfoRepository;
    private final ManagedSensorTypeService managedSensorTypeService;

    /**
     * 재배 환경 정보 이벤트({@link ThresholdInfoEvent})를 전달받아 등록, 수정, 삭제 처리를 수행합니다.
     * <ul>
     *     <li>{@code sensorRangeList.size() >= 4}: 신규 임계값 정보 등록 및 센서 타입 관리 목록 등록</li>
     *     <li>{@code sensorRangeList.size() == 1}: 기존 임계값 정보 특정 범주 수정</li>
     *     <li>{@code sensorRangeList.isEmpty()}: 임계값 정보 삭제</li>
     * </ul>
     *
     * @param thresholdInfoEvent 재배 환경 정보 이벤트 DTO
     */
    public void processThresholdInfoEvent(ThresholdInfoEvent thresholdInfoEvent) {
//        Long cultivationId = thresholdInfoEvent.getCultivationId();
//        List<SensorRange> sensorRangeList = thresholdInfoEvent.getSensorRangeList();
//
//        if (sensorRangeList.size() >= 4) {
//            validateDistinctSensorTypes(sensorRangeList);
//
//            ThresholdInfoDto newDto = ThresholdInfoDto.from(cultivationId, sensorRangeList);
//
//            thresholdInfoRepository.upsert(newDto);
//            log.debug("[ThresholdInfoService] 임계값 정보 신규 등록 : {}", newDto);
//
//            registerSensorTypes(sensorRangeList);
//
//            return;
//        }
//
//        if (sensorRangeList.size() == 1) {
//            ThresholdInfoDto toUpdate = thresholdInfoRepository.findByCultivationId(cultivationId)
//                    .orElseThrow(() -> new ThresholdInfoNotFoundException(cultivationId));
//
//            toUpdate.applyRange(sensorRangeList.getFirst());
//            thresholdInfoRepository.upsert(toUpdate);
//            log.debug("[ThresholdInfoService] 임계값 정보 수정 : {}", toUpdate);
//
//            registerSensorTypes(sensorRangeList);
//
//            return;
//        }
//
//        if (sensorRangeList.isEmpty()) {
//            thresholdInfoRepository.deleteByCultivationId(cultivationId);
//            log.debug("[ThresholdInfoService] 임계값 정보 삭제 : cultivationId={}", cultivationId);
//
//            return;
//        }
//
//        log.warn("[ThresholdInfoService] 추가/수정/삭제 중 어느 것도 이루어지지 않았습니다. {}", thresholdInfoEvent);
        Long cultivationId = thresholdInfoEvent.getCultivationId();
        List<SensorRange> sensorRangeList = thresholdInfoEvent.getSensorRangeList();

        if (sensorRangeList.isEmpty()) {
            thresholdInfoRepository.deleteByCultivationId(cultivationId);
            log.debug("[ThresholdInfoService] 임계값 정보 삭제 : cultivationId={}", cultivationId);
            return;
        }

        validateDistinctSensorTypes(sensorRangeList);

        ThresholdInfoDto dto = thresholdInfoRepository.findByCultivationId(cultivationId)
                .orElseGet(() -> ThresholdInfoDto.from(cultivationId, List.of()));

        sensorRangeList.forEach(dto::applyRange);

        thresholdInfoRepository.upsert(dto);
        log.debug("[ThresholdInfoService] 임계값 정보 등록/수정 : {}", dto);

        registerSensorTypes(sensorRangeList);
    }

    /**
     * 재배 환경 ID로 저장된 임계값 정보({@link ThresholdInfoDto})를 조회합니다.
     *
     * @param cultivationId 조회할 재배 환경의 ID
     * @return 조회된 재배 환경 임계값 정보 {@link ThresholdInfoDto}를 담은 {@link Optional} (존재하지 않는 경우 빈 Optional)
     */
    public Optional<ThresholdInfoDto> findCultivationInfo(Long cultivationId) {
        return thresholdInfoRepository.findByCultivationId(cultivationId);
    }

    // ======================================================================

    /**
     * 센서 범주 리스트에서 센서 타입을 추출하여 관리 대상 센서 타입 목록에 등록합니다.
     *
     * @param sensorRangeList 등록할 센서 범주 리스트
     */
    private void registerSensorTypes(List<SensorRange> sensorRangeList) {
        managedSensorTypeService.registerAll(
                sensorRangeList.stream()
                        .map(SensorRange::getSensorType)
                        .toList()
        );
    }

    /**
     * 센서 범주 리스트 내 (sensorType, unit) 조합의 중복 여부를 검증합니다.
     *
     * @param sensorRangeList 검증할 센서 범주 리스트
     * @throws IllegalArgumentException (sensorType, unit) 조합이 중복된 경우
     */
    private void validateDistinctSensorTypes(List<SensorRange> sensorRangeList) {
        long distinctCount = sensorRangeList.stream()
                .map(sensorRange -> "%s_%s".formatted(sensorRange.getSensorType(), sensorRange.getUnit()))
                .distinct()
                .count();

        if (distinctCount != sensorRangeList.size()) {
            throw new IllegalArgumentException(
                    "sensorRangeList에 (sensorType, unit) 조합이 중복되어 있습니다: %s".formatted(sensorRangeList)
            );
        }
    }

}
