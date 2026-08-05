package site.yesaido.ruleengine_server.collector.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import site.yesaido.ruleengine_server.collector.dto.SensorDataDto;

@AllArgsConstructor
@Getter
public class SensorDataReadyEvent {

    private final SensorDataDto sensorDataDto;
}
