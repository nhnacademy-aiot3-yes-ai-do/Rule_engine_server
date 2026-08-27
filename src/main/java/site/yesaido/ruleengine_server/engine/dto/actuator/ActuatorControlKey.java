package site.yesaido.ruleengine_server.engine.dto.actuator;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ActuatorControlKey {

    private Long cultivationId;

    private String sensorType;
}
