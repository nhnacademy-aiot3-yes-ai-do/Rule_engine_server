package site.yesaido.ruleengine_server.engine.rule.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import site.yesaido.ruleengine_server.collector.dto.SensorDataDto;
import site.yesaido.ruleengine_server.engine.dto.actuator.ActuatorControlKey;
import site.yesaido.ruleengine_server.engine.dto.actuator.ActuatorControlState;
import site.yesaido.ruleengine_server.engine.dto.actuator.ActuatorDirection;
import site.yesaido.ruleengine_server.engine.rule.Rule;
import site.yesaido.ruleengine_server.engine.service.ActuatorCommandExecutor;
import site.yesaido.ruleengine_server.engine.service.ActuatorControlStateService;
import site.yesaido.ruleengine_server.global.dto.SensorType;
import site.yesaido.ruleengine_server.global.dto.ThresholdInfoDto;
import site.yesaido.ruleengine_server.registry.dto.threshold.SensorRange;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Component
public class ActuatorControlRule implements Rule {

    private final Duration sustainedDuration;

    private final ActuatorControlStateService actuatorControlStateService;
    private final ActuatorCommandExecutor actuatorCommandExecutor;

    public ActuatorControlRule(@Value("${rule-engine.actuator.sustain.minutes}") long sustainedDuration,
                               ActuatorControlStateService actuatorControlStateService, ActuatorCommandExecutor actuatorCommandExecutor) {
        this.sustainedDuration = Duration.ofMinutes(sustainedDuration);
        this.actuatorControlStateService = actuatorControlStateService;
        this.actuatorCommandExecutor = actuatorCommandExecutor;
    }

    @Override
    public boolean supports(SensorDataDto dto) {

        return SensorType.contains(dto.getSensorType());
    }

    @Override
    public void evaluate(SensorDataDto sensorData, ThresholdInfoDto thresholdInfo) {

        SensorRange sensorRange = thresholdInfo.getRange(sensorData.getSensorType(), sensorData.getUnit());
        if (sensorRange == null) {
            return;
        }

        ActuatorControlKey key = new ActuatorControlKey(thresholdInfo.getCultivationId(), sensorData.getSensorType());
        ActuatorControlState currentState = actuatorControlStateService.getState(key);

        // cultivationId + sensorType으로 actuator동작 명령 상태를 조회했는데, PENDING일 경우 즉시 return
        if (currentState.getActuatorDirection() == ActuatorDirection.PENDING) {
            return;
        }

        ActuatorDirection targetDirection = determineDirection(sensorData.getValue(), sensorRange, currentState.getActuatorDirection());

        if (currentState.getActuatorDirection() == targetDirection) {
            return;
        }

        if (targetDirection == ActuatorDirection.NONE) {
            actuatorCommandExecutor.requestDirectionChange(key, currentState.getActuatorDirection(), targetDirection);

        } else {
            boolean isDirectionSwitch = currentState.getActuatorDirection() != ActuatorDirection.NONE;

            if (currentState.getExceededSince() == null || isDirectionSwitch) {
                actuatorControlStateService.resetExceededSince(key);
                return;
            }

            Duration exceededDuration = Duration.between(currentState.getExceededSince(), OffsetDateTime.now(ZoneOffset.UTC).withOffsetSameInstant(ZoneOffset.ofHours(9)));
            if (exceededDuration.compareTo(sustainedDuration) < 0) {
                return;
            }

            actuatorCommandExecutor.requestDirectionChange(key, currentState.getActuatorDirection(), targetDirection);
        }

    }

    private ActuatorDirection determineDirection(BigDecimal sensorValue, SensorRange sensorRange, ActuatorDirection currentDirection) {

        BigDecimal min = sensorRange.getMinValue();
        BigDecimal max = sensorRange.getMaxValue();

        if (sensorValue.compareTo(min) < 0) {
            return ActuatorDirection.INCREASING;
        }

        if (sensorValue.compareTo(max) > 0) {
            return ActuatorDirection.DECREASING;
        }

        BigDecimal midpoint = min.add(max).divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);

        if (currentDirection == ActuatorDirection.INCREASING && sensorValue.compareTo(midpoint) < 0) {
            return ActuatorDirection.INCREASING;
        }

        if (currentDirection == ActuatorDirection.DECREASING && sensorValue.compareTo(midpoint) > 0) {
            return ActuatorDirection.DECREASING;
        }

        return ActuatorDirection.NONE;
    }
}
