package site.yesaido.ruleengine_server.engine.dto.actuator;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ActuatorControlState {

    private ActuatorDirection actuatorDirection;

    private OffsetDateTime exceededSince;
}
