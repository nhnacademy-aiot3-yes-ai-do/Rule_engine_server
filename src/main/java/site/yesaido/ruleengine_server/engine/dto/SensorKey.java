package site.yesaido.ruleengine_server.engine.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SensorKey {

    private String deviceEui;

    private String sensorType;

    private String unit;
}
