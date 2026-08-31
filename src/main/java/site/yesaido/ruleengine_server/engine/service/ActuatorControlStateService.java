package site.yesaido.ruleengine_server.engine.service;

import site.yesaido.ruleengine_server.engine.dto.actuator.ActuatorControlKey;
import site.yesaido.ruleengine_server.engine.dto.actuator.ActuatorControlState;

public interface ActuatorControlStateService {

    ActuatorControlState getState(ActuatorControlKey key);

    void setState(ActuatorControlKey key, ActuatorControlState state);
}
