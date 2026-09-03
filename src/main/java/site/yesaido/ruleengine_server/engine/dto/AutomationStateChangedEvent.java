package site.yesaido.ruleengine_server.engine.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AutomationStateChangedEvent {

    private UUID eventId;
    private long cultivationId;
    private String actuatorType;
    private String message;
    private boolean enabled;
    private OffsetDateTime occurredAt;
}
