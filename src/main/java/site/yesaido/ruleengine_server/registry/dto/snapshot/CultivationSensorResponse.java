package site.yesaido.ruleengine_server.registry.dto.snapshot;

import java.util.List;

public record CultivationSensorResponse(
        long cultivationId,
        String deviceEui,
        String deviceName,
        String location,
        String locationDetail,
        String deviceModel,
        List<CultivationSensorTypeResponse> sensorTypes
) {
}