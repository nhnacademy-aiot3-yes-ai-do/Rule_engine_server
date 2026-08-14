package site.yesaido.ruleengine_server.engine.service;

import site.yesaido.ruleengine_server.engine.dto.SensorKey;

public interface AlertCooldownService {

    boolean canAlert(SensorKey sensorKey);

    void recordAlert(SensorKey sensorKey);

    void resetCooldown(SensorKey sensorKey);

}
