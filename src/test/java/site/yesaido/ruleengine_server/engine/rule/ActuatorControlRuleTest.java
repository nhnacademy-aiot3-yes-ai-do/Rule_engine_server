package site.yesaido.ruleengine_server.engine.rule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.yesaido.ruleengine_server.collector.dto.SensorDataDto;
import site.yesaido.ruleengine_server.engine.dto.actuator.*;
import site.yesaido.ruleengine_server.engine.rule.impl.ActuatorControlRule;
import site.yesaido.ruleengine_server.engine.service.ActuatorCommandExecutor;
import site.yesaido.ruleengine_server.engine.service.ActuatorControlStateService;
import site.yesaido.ruleengine_server.engine.service.ActuatorTargetSinceService;
import site.yesaido.ruleengine_server.engine.service.SensorValueAverageService;
import site.yesaido.ruleengine_server.global.dto.ThresholdInfoDto;
import site.yesaido.ruleengine_server.registry.dto.threshold.SensorRange;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActuatorControlRuleTest {

    private static final Long CULTIVATION_ID = 10L;
    private static final String SENSOR_TYPE = "TEMPERATURE";

    @Mock
    private SensorValueAverageService sensorValueAverageService;
    @Mock
    private ActuatorControlStateService actuatorControlStateService;
    @Mock
    private ActuatorTargetSinceService actuatorTargetSinceService;
    @Mock
    private ActuatorCommandExecutor actuatorCommandExecutor;

    private ActuatorControlRule rule;

    private SensorDataDto sensorData;
    private ThresholdInfoDto thresholdInfo;
    private ActuatorControlKey key;

    @BeforeEach
    void setUp() {
        rule = new ActuatorControlRule(1L,
                sensorValueAverageService, actuatorControlStateService, actuatorTargetSinceService, actuatorCommandExecutor);

        sensorData = new SensorDataDto(
                "실습실", "후면 오른쪽",
                "AM107", "AM107-067999", "24e124128c067999",
                SENSOR_TYPE,
                BigDecimal.valueOf(23.7),
                OffsetDateTime.now(),
                "°C",
                CULTIVATION_ID
        );

        SensorRange sensorRange = new SensorRange(SENSOR_TYPE, "°C", BigDecimal.valueOf(20.0), BigDecimal.valueOf(30.0));
        thresholdInfo = ThresholdInfoDto.from(CULTIVATION_ID, List.of(sensorRange));

        key = new ActuatorControlKey(CULTIVATION_ID, SENSOR_TYPE);
    }

    @Test
    void test_supports_whenValidSensorType_returnsTrue() {
        assertTrue(rule.supports(sensorData));
    }

    @Test
    void test_supports_whenInvalidSensorType_returnsFalse() {
        assertFalse(rule.supports(withSensorType(sensorData, "SOIL_PH")));
    }

    @Test
    void test_evaluate_whenSensorRangeNotFound_doesNothing() {
        ThresholdInfoDto emptyThresholdInfo = ThresholdInfoDto.from(CULTIVATION_ID, List.of());

        rule.evaluate(sensorData, emptyThresholdInfo);

        verifyNoInteractions(sensorValueAverageService, actuatorControlStateService, actuatorTargetSinceService, actuatorCommandExecutor);
    }

    @Test
    void test_evaluate_alwaysWritesToAverageCache_regardlessOfState() {
        when(actuatorControlStateService.getState(eq(key))).thenReturn(new ActuatorControlState.Pending());

        rule.evaluate(sensorData, thresholdInfo);

        verify(sensorValueAverageService, times(1))
                .put(eq(new SensorValueKey(CULTIVATION_ID, SENSOR_TYPE, sensorData.getDeviceEui())), eq(sensorData.getValue()));
    }

    @Test
    void test_evaluate_whenPending_doesNothing() {
        when(actuatorControlStateService.getState(eq(key))).thenReturn(new ActuatorControlState.Pending());

        rule.evaluate(sensorData, thresholdInfo);

        verifyNoInteractions(actuatorCommandExecutor);
        verify(actuatorControlStateService, never()).setState(any(), any());
        verifyNoInteractions(actuatorTargetSinceService);
    }

    @Test
    void test_evaluate_whenNoneAndWithinRange_clearsTargetSinceAndDoesNothing() {
        SensorDataDto normalData = withValue(sensorData, BigDecimal.valueOf(24.0));
        when(actuatorControlStateService.getState(eq(key))).thenReturn(new ActuatorControlState.None());
        when(sensorValueAverageService.getAverage(CULTIVATION_ID, SENSOR_TYPE)).thenReturn(BigDecimal.valueOf(24.0));

        rule.evaluate(normalData, thresholdInfo);

        verify(actuatorTargetSinceService, times(1)).clear(key);
        verifyNoInteractions(actuatorCommandExecutor);
        verify(actuatorControlStateService, never()).setState(any(), any());
    }

    @Test
    void test_evaluate_whenNoneAndFirstExceeded_recordsTargetSinceWithoutExecuting() {
        SensorDataDto belowMinData = withValue(sensorData, BigDecimal.valueOf(15.0));
        when(actuatorControlStateService.getState(eq(key))).thenReturn(new ActuatorControlState.None());
        when(sensorValueAverageService.getAverage(CULTIVATION_ID, SENSOR_TYPE)).thenReturn(BigDecimal.valueOf(15.0));
        when(actuatorTargetSinceService.getTargetSince(key)).thenReturn(null);

        rule.evaluate(belowMinData, thresholdInfo);

        verify(actuatorTargetSinceService, times(1))
                .setTargetSince(eq(key), argThat(ts -> ts.target() == ActuatorType.HEATER));
        verifyNoInteractions(actuatorCommandExecutor);
    }

    @Test
    void test_evaluate_whenNoneAndNotYetSustained_doesNothing() {
        SensorDataDto belowMinData = withValue(sensorData, BigDecimal.valueOf(15.0));
        when(actuatorControlStateService.getState(eq(key))).thenReturn(new ActuatorControlState.None());
        when(sensorValueAverageService.getAverage(CULTIVATION_ID, SENSOR_TYPE)).thenReturn(BigDecimal.valueOf(15.0));
        when(actuatorTargetSinceService.getTargetSince(key))
                .thenReturn(new ActuatorTargetSinceService.TargetSince(ActuatorType.HEATER, Instant.now().minusSeconds(10)));

        rule.evaluate(belowMinData, thresholdInfo);

        verify(actuatorTargetSinceService, never()).setTargetSince(any(), any());
        verifyNoInteractions(actuatorCommandExecutor);
        verify(actuatorControlStateService, never()).setState(any(), any());
    }

    @Test
    void test_evaluate_whenNoneAndSustainedLongEnough_startsSuccessfully() {
        SensorDataDto belowMinData = withValue(sensorData, BigDecimal.valueOf(15.0));
        when(actuatorControlStateService.getState(eq(key))).thenReturn(new ActuatorControlState.None());
        when(sensorValueAverageService.getAverage(CULTIVATION_ID, SENSOR_TYPE)).thenReturn(BigDecimal.valueOf(15.0));
        when(actuatorTargetSinceService.getTargetSince(key))
                .thenReturn(new ActuatorTargetSinceService.TargetSince(ActuatorType.HEATER, Instant.now().minusSeconds(90)));
        when(actuatorCommandExecutor.sendCommand(key, ActuatorType.HEATER, ActuatorState.ON)).thenReturn(true);

        rule.evaluate(belowMinData, thresholdInfo);

        verify(actuatorControlStateService).setState(key, new ActuatorControlState.Pending());
        verify(actuatorControlStateService).setState(key, new ActuatorControlState.Active(ActuatorType.HEATER));
        verify(actuatorTargetSinceService, times(1)).clear(key);
    }

    @Test
    void test_evaluate_whenNoneAndSustainedButCommandFails_revertsToNone() {
        SensorDataDto belowMinData = withValue(sensorData, BigDecimal.valueOf(15.0));
        when(actuatorControlStateService.getState(eq(key))).thenReturn(new ActuatorControlState.None());
        when(sensorValueAverageService.getAverage(CULTIVATION_ID, SENSOR_TYPE)).thenReturn(BigDecimal.valueOf(15.0));
        when(actuatorTargetSinceService.getTargetSince(key))
                .thenReturn(new ActuatorTargetSinceService.TargetSince(ActuatorType.HEATER, Instant.now().minusSeconds(90)));
        when(actuatorCommandExecutor.sendCommand(key, ActuatorType.HEATER, ActuatorState.ON)).thenReturn(false);

        rule.evaluate(belowMinData, thresholdInfo);

        verify(actuatorControlStateService).setState(key, new ActuatorControlState.Pending());
        verify(actuatorControlStateService).setState(key, new ActuatorControlState.None());
        verify(actuatorTargetSinceService, never()).clear(key);
    }

    @Test
    void test_evaluate_whenActiveAndStillNeeded_doesNothing() {
        SensorDataDto belowMinData = withValue(sensorData, BigDecimal.valueOf(15.0));
        when(actuatorControlStateService.getState(eq(key))).thenReturn(new ActuatorControlState.Active(ActuatorType.HEATER));
        when(sensorValueAverageService.getAverage(CULTIVATION_ID, SENSOR_TYPE)).thenReturn(BigDecimal.valueOf(15.0));

        rule.evaluate(belowMinData, thresholdInfo);

        verifyNoInteractions(actuatorCommandExecutor);
        verify(actuatorControlStateService, never()).setState(any(), any());
    }

    @Test
    void test_evaluate_whenActiveAndPastMidpoint_stopsImmediately() {
        SensorDataDto recoveredData = withValue(sensorData, BigDecimal.valueOf(27.0));
        when(actuatorControlStateService.getState(eq(key))).thenReturn(new ActuatorControlState.Active(ActuatorType.HEATER));
        when(sensorValueAverageService.getAverage(CULTIVATION_ID, SENSOR_TYPE)).thenReturn(BigDecimal.valueOf(27.0));
        when(actuatorCommandExecutor.sendCommand(key, ActuatorType.HEATER, ActuatorState.OFF)).thenReturn(true);

        rule.evaluate(recoveredData, thresholdInfo);

        verify(actuatorControlStateService).setState(key, new ActuatorControlState.Pending());
        verify(actuatorControlStateService).setState(key, new ActuatorControlState.None());
    }

    @Test
    void test_evaluate_whenActiveAndOppositeNeeded_stopsCurrentOnlyWithoutStartingOpposite() {
        SensorDataDto exceedsMaxData = withValue(sensorData, BigDecimal.valueOf(35.0));
        when(actuatorControlStateService.getState(eq(key))).thenReturn(new ActuatorControlState.Active(ActuatorType.HEATER));
        when(sensorValueAverageService.getAverage(CULTIVATION_ID, SENSOR_TYPE)).thenReturn(BigDecimal.valueOf(35.0));
        when(actuatorCommandExecutor.sendCommand(key, ActuatorType.HEATER, ActuatorState.OFF)).thenReturn(true);

        rule.evaluate(exceedsMaxData, thresholdInfo);

        verify(actuatorCommandExecutor, times(1)).sendCommand(key, ActuatorType.HEATER, ActuatorState.OFF);
        verify(actuatorCommandExecutor, never()).sendCommand(eq(key), eq(ActuatorType.COOLER), any());
        verify(actuatorControlStateService).setState(key, new ActuatorControlState.None());
    }

    @Test
    void test_evaluate_whenStopCommandFails_revertsToOriginalActiveType() {
        SensorDataDto recoveredData = withValue(sensorData, BigDecimal.valueOf(27.0));
        when(actuatorControlStateService.getState(eq(key))).thenReturn(new ActuatorControlState.Active(ActuatorType.HEATER));
        when(sensorValueAverageService.getAverage(CULTIVATION_ID, SENSOR_TYPE)).thenReturn(BigDecimal.valueOf(27.0));
        when(actuatorCommandExecutor.sendCommand(key, ActuatorType.HEATER, ActuatorState.OFF)).thenReturn(false);

        rule.evaluate(recoveredData, thresholdInfo);

        verify(actuatorControlStateService).setState(key, new ActuatorControlState.Pending());
        verify(actuatorControlStateService).setState(key, new ActuatorControlState.Active(ActuatorType.HEATER));
        verify(actuatorControlStateService, never()).setState(key, new ActuatorControlState.None());
    }

    // ======================================================================

    private SensorDataDto withValue(SensorDataDto base, BigDecimal value) {
        return new SensorDataDto(
                base.getPlace(), base.getLocation(),
                base.getDeviceModel(), base.getDeviceName(), base.getDeviceEui(),
                base.getSensorType(),
                value,
                base.getTime(),
                base.getUnit(),
                base.getCultivationId()
        );
    }

    private SensorDataDto withSensorType(SensorDataDto base, String sensorType) {
        return new SensorDataDto(
                base.getPlace(), base.getLocation(),
                base.getDeviceModel(), base.getDeviceName(), base.getDeviceEui(),
                sensorType,
                base.getValue(),
                base.getTime(),
                base.getUnit(),
                base.getCultivationId()
        );
    }
}
