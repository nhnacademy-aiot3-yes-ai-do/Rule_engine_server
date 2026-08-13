package site.yesaido.ruleengine_server.engine.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import site.yesaido.ruleengine_server.collector.dto.SensorDataDto;
import site.yesaido.ruleengine_server.collector.service.CollectorService;
import site.yesaido.ruleengine_server.global.dto.ThresholdInfoDto;
import site.yesaido.ruleengine_server.registry.dto.threshold.ThresholdInfoEvent;
import site.yesaido.ruleengine_server.registry.service.ThresholdInfoService;

import java.util.Optional;

/**
 * {@link CollectorService}로부터 전달받은 센서 데이터를 , 해당 재배 환경에 설정된 임계값과 비교하여 정상 범우인지 이상 상황인지 판단을 담당하는 <strong>RuleEngine</strong>입니다.<br>
 * 1. 센서 데이터에 담겨있는 {@code  cultivationId}를 활용하여 재배 환경 정보를 얻어옵니다.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class RuleEngine {

    private final ThresholdInfoService thresholdInfoService;

    public void start(SensorDataDto dto) {
         Optional<ThresholdInfoDto> cultivationInfoDtoOptional =
                 thresholdInfoService.findCultivationInfo(dto.getCultivationId());

         if (cultivationInfoDtoOptional.isEmpty()) {
             log.warn("임계값 정보 없음 - 판단 보류: cultivationId={}", dto.getCultivationId());
             return;
         }

         publishRealTimeData(dto);

    }

    private void publishRealTimeData(SensorDataDto dto) {
        log.debug("[RuleEngine] 발행 테스트: {}", dto);
    }
}
