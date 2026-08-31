package site.yesaido.ruleengine_server.engine.dto.actuator;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ActuatorControlKey {

    private Long cultivationId;

    private String sensorType;
}
