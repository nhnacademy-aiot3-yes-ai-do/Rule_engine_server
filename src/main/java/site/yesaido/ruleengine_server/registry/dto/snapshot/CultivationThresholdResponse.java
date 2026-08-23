package site.yesaido.ruleengine_server.registry.dto.snapshot;

import java.math.BigDecimal;

public record CultivationThresholdResponse(
        long cultivationId,
        String sensorType,
        String unit,
        BigDecimal minValue,
        BigDecimal maxValue
) {
}