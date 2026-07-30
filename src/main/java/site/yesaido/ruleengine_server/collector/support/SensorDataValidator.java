package site.yesaido.ruleengine_server.collector.support;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import site.yesaido.ruleengine_server.collector.dto.SensorDataDto;

@Slf4j
@Component
public class SensorDataValidator {

    public boolean isValid(SensorDataDto sensorDataDto) {

        // todo : valid 구현

        // valid == false 인 경우
        log.info("valid == false : {}", sensorDataDto);

        return true;
    }
}
