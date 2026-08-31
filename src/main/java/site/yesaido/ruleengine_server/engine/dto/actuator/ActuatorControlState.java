package site.yesaido.ruleengine_server.engine.dto.actuator;

public sealed interface ActuatorControlState {

    record None() implements ActuatorControlState {}

    record Pending() implements ActuatorControlState {}

    record Active(ActuatorType actuatorType) implements ActuatorControlState {}
}
