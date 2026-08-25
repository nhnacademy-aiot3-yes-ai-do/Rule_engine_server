package site.yesaido.ruleengine_server.engine.dto.actuator;

import java.time.Instant;
import java.util.UUID;

public record ActuatorCommandRequest(

        UUID controlId,
        UUID commandId,
        ActuatorState desiredState,
        Instant requestedAt,
        Instant expiresAt
) {
}
