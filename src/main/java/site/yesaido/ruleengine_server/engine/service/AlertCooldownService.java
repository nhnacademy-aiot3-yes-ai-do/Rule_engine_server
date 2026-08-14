package site.yesaido.ruleengine_server.engine.service;

import site.yesaido.ruleengine_server.engine.dto.SensorKey;

public interface AlertCooldownService {

    // 쿨다운 중인지 (초과 알림 스팸 방지용)
    boolean canAlert(SensorKey sensorKey);

    // 쿨다운 타이머 시작
    void recordAlert(SensorKey sensorKey);

    // 현재 초과 상태로 기록되어 있는지
    boolean isCurrentlyExceeded(SensorKey sensorKey);

    // 초과 상태로 표시
    void markExceeded(SensorKey sensorKey);

    // 정상 상태로 표시
    void markNormal(SensorKey sensorKey);

}
