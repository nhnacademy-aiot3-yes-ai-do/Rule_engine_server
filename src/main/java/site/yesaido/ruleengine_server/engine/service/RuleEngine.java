package site.yesaido.ruleengine_server.engine.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import site.yesaido.ruleengine_server.collector.dto.SensorDataDto;
import site.yesaido.ruleengine_server.registry.service.CultivationInfoService;

@RequiredArgsConstructor
@Service
public class RuleEngine {

    private final CultivationInfoService cultivationInfoService;

    public void start(SensorDataDto sensorDataDto) {
        // Optional<CultivationInfoDto> cultivationInfoDtoOptional = cultivationInfoService.
    }
}
