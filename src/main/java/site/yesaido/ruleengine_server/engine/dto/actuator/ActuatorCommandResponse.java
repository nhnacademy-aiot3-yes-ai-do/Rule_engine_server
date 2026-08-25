package site.yesaido.ruleengine_server.engine.dto.actuator;

import java.time.Instant;
import java.util.UUID;

public record ActuatorCommandResponse(

        UUID controlId,
        UUID commandId,
        ActuatorCommandStatus status,
        ActuatorState actualState,
        Instant appliedAt
) {
}
