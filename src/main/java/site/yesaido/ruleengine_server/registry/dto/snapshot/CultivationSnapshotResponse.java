package site.yesaido.ruleengine_server.registry.dto.snapshot;

import java.time.OffsetDateTime;
import java.util.List;

public record CultivationSnapshotResponse(
        OffsetDateTime snapshotAt,
        List<CultivationSensorResponse> sensors,
        List<CultivationThresholdResponse> thresholds
) {
}