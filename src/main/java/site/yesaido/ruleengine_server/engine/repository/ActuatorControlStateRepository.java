package site.yesaido.ruleengine_server.engine.repository;

import site.yesaido.ruleengine_server.engine.dto.actuator.ActuatorControlKey;
import site.yesaido.ruleengine_server.engine.dto.actuator.ActuatorControlState;

import java.util.Optional;

public interface ActuatorControlStateRepository {

    Optional<ActuatorControlState> find(ActuatorControlKey key);

    void save(ActuatorControlKey key, ActuatorControlState state);
}
