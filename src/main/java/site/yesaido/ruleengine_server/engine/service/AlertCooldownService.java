package site.yesaido.ruleengine_server.engine.service;

import site.yesaido.ruleengine_server.engine.dto.SensorKey;

public interface AlertCooldownService {

    boolean tryMarkExceeded(SensorKey sensorKey);

    boolean tryMarkNormal(SensorKey sensorKey);
}
