package site.yesaido.ruleengine_server.engine.core;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import site.yesaido.ruleengine_server.collector.dto.SensorDataDto;
import site.yesaido.ruleengine_server.collector.service.CollectorService;
import site.yesaido.ruleengine_server.engine.rule.Rule;
import site.yesaido.ruleengine_server.engine.service.InfluxService;
import site.yesaido.ruleengine_server.global.dto.SensorValueEvent;
import site.yesaido.ruleengine_server.global.dto.ThresholdInfoDto;
import site.yesaido.ruleengine_server.registry.service.ThresholdInfoService;

import java.util.List;
import java.util.Optional;

/**
 * {@link CollectorService}로부터 전달받은 센서 데이터를, 해당 재배 환경에 설정된 임계값과 비교하여 정상 범위인지 이상 상황인지 판단을 담당하는 <strong>RuleEngine</strong>입니다.<br>
 * 1. 센서 데이터에 담겨있는 {@code cultivationId}를 활용하여 재배 환경의 임계값 정보를 조회합니다. 정보가 없으면 판단을 보류합니다.<br>
 * 2. 조회에 성공하면 센서 데이터를 InfluxDB에 실시간 데이터로 저장합니다.<br>
 * 3. 등록된 {@link Rule} 목록을 순회하며, 해당 센서 데이터를 지원하는 Rule에 대해 평가를 수행합니다.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class RuleEngine {

    private final ThresholdInfoService thresholdInfoService;
    private final InfluxService influxService;
    private final List<Rule> rules;

    public void start(SensorDataDto dto) {

         Optional<ThresholdInfoDto> cultivationInfoDtoOptional = thresholdInfoService.findCultivationInfo(dto.getCultivationId());

         if (cultivationInfoDtoOptional.isEmpty()) {
             log.warn("임계값 정보 없음 - 판단 보류: cultivationId={}", dto.getCultivationId());
             return;
         }

         publishRealTimeData(dto);

         ThresholdInfoDto thresholdInfoDto = cultivationInfoDtoOptional.get();
         rules.stream()
                .filter(rule -> rule.supports(dto))
                .forEach(rule -> rule.evaluate(dto, thresholdInfoDto));
    }

    private void publishRealTimeData(SensorDataDto dto) {
        influxService.save(SensorValueEvent.from(dto));
    }
}
