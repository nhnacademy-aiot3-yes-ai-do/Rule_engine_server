package site.yesaido.ruleengine_server.registry.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import site.yesaido.ruleengine_server.global.dto.ThresholdInfoDto;
import site.yesaido.ruleengine_server.global.exception.ThresholdInfoNotFoundException;
import site.yesaido.ruleengine_server.registry.dto.threshold.SensorRange;
import site.yesaido.ruleengine_server.registry.dto.threshold.ThresholdInfoEvent;
import site.yesaido.ruleengine_server.registry.repository.ThresholdInfoRepository;

import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class ThresholdInfoService {

    private final ThresholdInfoRepository thresholdInfoRepository;

    public void processThresholdInfoEvent(ThresholdInfoEvent thresholdInfoEvent) {
        Long cultivationId = thresholdInfoEvent.getCultivationId();
        List<SensorRange> sensorRangeList = thresholdInfoEvent.getSensorRangeList();

        if (sensorRangeList.size() == 4) {
            ThresholdInfoDto newDto = ThresholdInfoDto.from(cultivationId, sensorRangeList);

            thresholdInfoRepository.upsert(newDto);
            log.debug("[ThresholdInfoService] 임계값 정보 신규 등록 : {}", newDto);

            return;
        }

        if (sensorRangeList.size() == 1) {
            ThresholdInfoDto toUpdate = thresholdInfoRepository.findByCultivationId(cultivationId)
                    .orElseThrow(() -> new ThresholdInfoNotFoundException(cultivationId));

            toUpdate.applyRange(sensorRangeList.getFirst());
            thresholdInfoRepository.upsert(toUpdate);
            log.debug("[ThresholdInfoService] 임계값 정보 수정 : {}", toUpdate);

            return;
        }

        if (sensorRangeList.isEmpty()) {
            thresholdInfoRepository.deleteByCultivationId(cultivationId);
            return;
        }

        log.warn("");
    }

    public Optional<ThresholdInfoDto> findCultivationInfo(Long cultivationId) {
        return thresholdInfoRepository.findByCultivationId(cultivationId);
    }

}
