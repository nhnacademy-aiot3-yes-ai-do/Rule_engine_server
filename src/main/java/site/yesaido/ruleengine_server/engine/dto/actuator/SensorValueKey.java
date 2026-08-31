package site.yesaido.ruleengine_server.engine.dto.actuator;

public record SensorValueKey(
        Long cultivationId,
        String sensorType,
        String deviceEui
) {
}
