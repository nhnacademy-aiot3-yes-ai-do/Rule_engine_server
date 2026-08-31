package site.yesaido.ruleengine_server.engine.rule.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import site.yesaido.ruleengine_server.collector.dto.SensorDataDto;
import site.yesaido.ruleengine_server.engine.dto.actuator.*;
import site.yesaido.ruleengine_server.engine.rule.Rule;
import site.yesaido.ruleengine_server.engine.service.ActuatorCommandExecutor;
import site.yesaido.ruleengine_server.engine.service.ActuatorControlStateService;
import site.yesaido.ruleengine_server.engine.service.ActuatorTargetSinceService;
import site.yesaido.ruleengine_server.engine.service.SensorValueAverageService;
import site.yesaido.ruleengine_server.engine.support.SensorUnitConverter;
import site.yesaido.ruleengine_server.global.dto.SensorType;
import site.yesaido.ruleengine_server.global.dto.ThresholdInfoDto;
import site.yesaido.ruleengine_server.registry.dto.threshold.SensorRange;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class ActuatorControlRule implements Rule {

    private final Duration sustainedDuration;

    private final SensorValueAverageService sensorValueAverageService;
    private final ActuatorControlStateService actuatorControlStateService;
    private final ActuatorTargetSinceService actuatorTargetSinceService;
    private final ActuatorCommandExecutor actuatorCommandExecutor;

    private final Map<ActuatorControlKey, Lock> locks = new ConcurrentHashMap<>();

    public ActuatorControlRule(@Value("${rule-engine.actuator.sustain.minutes}") long sustainedMinutes,
                               SensorValueAverageService sensorValueAverageService, ActuatorControlStateService actuatorControlStateService, ActuatorTargetSinceService actuatorTargetSinceService, ActuatorCommandExecutor actuatorCommandExecutor) {
        this.sustainedDuration = Duration.ofMinutes(sustainedMinutes);
        this.sensorValueAverageService = sensorValueAverageService;
        this.actuatorControlStateService = actuatorControlStateService;
        this.actuatorTargetSinceService = actuatorTargetSinceService;
        this.actuatorCommandExecutor = actuatorCommandExecutor;
    }

    @Override
    public boolean supports(SensorDataDto dto) {

        return SensorType.contains(dto.getSensorType());
    }

    @Override
    public void evaluate(SensorDataDto sensorData, ThresholdInfoDto thresholdInfo) {

        SensorRange range = thresholdInfo.findRangeBySensorType(sensorData.getSensorType());
        if(range == null) {
            return;
        }

        BigDecimal convertValue = SensorUnitConverter.convert(sensorData.getValue(), sensorData.getUnit(), range.getUnit());

        SensorValueKey sensorValueKey = new SensorValueKey(
                thresholdInfo.getCultivationId(),
                sensorData.getSensorType(),
                sensorData.getDeviceEui()
        );
        sensorValueAverageService.put(sensorValueKey, convertValue);

        ActuatorControlKey key = new ActuatorControlKey(thresholdInfo.getCultivationId(), sensorData.getSensorType());

        Lock lock = locks.computeIfAbsent(key, k -> new ReentrantLock());
        if (!lock.tryLock()) {
            return;
        }
        try {
            ActuatorControlState currentState = actuatorControlStateService.getState(key);

            if (currentState instanceof ActuatorControlState.Pending) {
                return;
            }

            BigDecimal average = sensorValueAverageService.getAverage(thresholdInfo.getCultivationId(), sensorData.getSensorType());

            ActuatorType target = determineType(average, range, currentState);

            if (currentState instanceof ActuatorControlState.None) {
                if (target == null) {
                    actuatorTargetSinceService.clear(key);
                    return;   // 3-a: 계속 아무것도 안 켜진 상태 유지
                }
                // 4순위: 지속시간 확인 후 시작
                startIfSustained(key, target);
                return;
            }

            if (currentState instanceof ActuatorControlState.Active active) {
                if (target != null && active.actuatorType() == target) {
                    return;   // 3-a: 계속 같은 게 켜진 상태 유지
                }
                // 3-b/5순위 통합: 현재≠목표이니 즉시 정지
                stopImmediately(key, active.actuatorType());
            }
        } finally {
            lock.unlock();
        }

    }

    // ======================================================================

    private ActuatorType determineType(BigDecimal average, SensorRange range, ActuatorControlState currentState) {

        if (average.compareTo(range.getMinValue()) < 0) {
            return ActuatorType.increasingTypeOf(range.getSensorType());
        }
        if (average.compareTo(range.getMaxValue()) > 0) {
            return ActuatorType.decreasingTypeOf(range.getSensorType());
        }

        if (currentState instanceof ActuatorControlState.Active active) {
            BigDecimal midpoint = range.getMinValue().add(range.getMaxValue())
                    .divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);

            if (active.actuatorType().isIncreasing() && average.compareTo(midpoint) < 0) {
                return active.actuatorType();
            }
            if (!active.actuatorType().isIncreasing() && average.compareTo(midpoint) > 0) {
                return active.actuatorType();
            }
        }

        return null;
    }

    private void stopImmediately(ActuatorControlKey key, ActuatorType currentType) {

        actuatorControlStateService.setState(key, new ActuatorControlState.Pending());

        ActuatorCommandResult result = actuatorCommandExecutor.sendCommand(key, currentType, ActuatorState.OFF);

        // OFF 요청은 Data_generator의 반대 액추에이터 충돌 검사 대상이 아니므로 REJECTED_CONFLICT가 나올 수 없음
        if (result == ActuatorCommandResult.APPLIED) {
            actuatorControlStateService.setState(key, new ActuatorControlState.None());
        } else {
            actuatorControlStateService.setState(key, new ActuatorControlState.Active(currentType));
        }
    }

    private void startIfSustained(ActuatorControlKey key, ActuatorType target) {

        ActuatorTargetSinceService.TargetSince targetSince = actuatorTargetSinceService.getTargetSince(key);

        if (targetSince == null || targetSince.target() != target) {
            actuatorTargetSinceService.setTargetSince(key, new ActuatorTargetSinceService.TargetSince(target, Instant.now()));
            return;
        }

        Duration elapsed = Duration.between(targetSince.since(), Instant.now());
        if (elapsed.compareTo(sustainedDuration) < 0) {
            return;
        }

        actuatorControlStateService.setState(key, new ActuatorControlState.Pending());

        ActuatorCommandResult result = actuatorCommandExecutor.sendCommand(key, target, ActuatorState.ON);

        switch (result) {
            case APPLIED -> {
                actuatorControlStateService.setState(key, new ActuatorControlState.Active(target));
                actuatorTargetSinceService.clear(key);
            }
            // 반대쪽이 실제로는 켜져 있다는 뜻이므로, 내 기록을 그 반대쪽으로 정정 (7번 자기치유).
            // 여기서 곧바로 재시도하지 않고, 다음 센서 이벤트가 정상 절차(즉시 정지 -> 재시작)를 밟도록 둔다.
            case REJECTED_CONFLICT -> actuatorControlStateService.setState(key, new ActuatorControlState.Active(target.getOppositeType()));
            case FAILED -> actuatorControlStateService.setState(key, new ActuatorControlState.None());
        }
    }
}
