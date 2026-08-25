package site.yesaido.ruleengine_server.engine.support;

import site.yesaido.ruleengine_server.engine.dto.actuator.ActuatorDirection;

public class ActuatorTypeResolver {

    private ActuatorTypeResolver() {}

    public static String resolve(String sensorType, ActuatorDirection direction) {

        if (direction == ActuatorDirection.NONE || direction == ActuatorDirection.PENDING) {
            throw new IllegalArgumentException("resolve()는 INCREASING/DECREASING에 대해서만 호출해야 합니다: " + direction);
        }

        return switch (sensorType) {
            case "TEMPERATURE" -> direction == ActuatorDirection.INCREASING ? "HEATER" : "COOLER";
            case "HUMIDITY" -> direction == ActuatorDirection.INCREASING ? "HUMIDIFIER" : "DEHUMIDIFIER";
            case "CO2" -> direction == ActuatorDirection.INCREASING ? "CO2_SUPPLIER" : "VENTILATION_FAN";
            case "LIGHT" -> direction == ActuatorDirection.INCREASING ? "LED" : "LIGHT_REDUCER";
            default -> throw new IllegalArgumentException("하지 않는 센터 타입: " + sensorType);
        };
    }
}
