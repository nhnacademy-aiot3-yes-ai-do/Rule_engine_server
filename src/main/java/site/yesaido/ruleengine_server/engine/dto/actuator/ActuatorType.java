package site.yesaido.ruleengine_server.engine.dto.actuator;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@AllArgsConstructor
@Getter
public enum ActuatorType {

    HEATER("TEMPERATURE", true),
    COOLER("TEMPERATURE", false),

    HUMIDIFIER("HUMIDITY", true),
    DEHUMIDIFIER("HUMIDITY", false),

    CO2_SUPPLIER("CO2", true),
    VENTILATION_FAN("CO2", false),

    LED("LIGHT", true),
    LIGHT_REDUCER("LIGHT", false);

    private final String targetSensorType;
    private final boolean increasing;

    public static ActuatorType increasingTypeOf(String sensorType) {
        return findByTargetSensorType(sensorType, true);
    }

    public static ActuatorType decreasingTypeOf(String sensorType) {
        return findByTargetSensorType(sensorType, false);
    }

    private static ActuatorType findByTargetSensorType(String sensorType, boolean increasing) {

        return Arrays.stream(values())
                .filter(type -> type.getTargetSensorType().equals(sensorType) && type.isIncreasing() == increasing)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 센서 타입: %s".formatted(sensorType)));
    }

    public ActuatorType getOppositeType() {
        return findByTargetSensorType(targetSensorType, !increasing);
    }
}
