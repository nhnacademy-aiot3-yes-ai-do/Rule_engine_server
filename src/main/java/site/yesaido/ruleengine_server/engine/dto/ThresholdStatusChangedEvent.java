package site.yesaido.ruleengine_server.engine.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import site.yesaido.ruleengine_server.collector.dto.SensorDataDto;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ThresholdStatusChangedEvent {

    private UUID eventId;
    private SensorDataDto sensorData;
    private ThresholdStatus status;
    private OffsetDateTime occurredAt;
}
