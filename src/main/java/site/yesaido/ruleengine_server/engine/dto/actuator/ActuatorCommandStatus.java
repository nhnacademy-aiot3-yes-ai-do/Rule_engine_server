package site.yesaido.ruleengine_server.engine.dto.actuator;

public enum ActuatorCommandStatus {

    APPLIED,
    REJECTED_EXPIRED,
    REJECTED_STALE,
    REJECTED_CONFLICT
}
