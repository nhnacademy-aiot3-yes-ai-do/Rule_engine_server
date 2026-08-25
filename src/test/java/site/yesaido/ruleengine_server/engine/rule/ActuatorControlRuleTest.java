package site.yesaido.ruleengine_server.engine.rule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.yesaido.ruleengine_server.collector.dto.SensorDataDto;
import site.yesaido.ruleengine_server.engine.dto.actuator.ActuatorControlKey;
import site.yesaido.ruleengine_server.engine.dto.actuator.ActuatorControlState;
import site.yesaido.ruleengine_server.engine.dto.actuator.ActuatorDirection;
import site.yesaido.ruleengine_server.engine.rule.impl.ActuatorControlRule;
import site.yesaido.ruleengine_server.engine.service.ActuatorCommandExecutor;
import site.yesaido.ruleengine_server.engine.service.ActuatorControlStateService;
import site.yesaido.ruleengine_server.global.dto.ThresholdInfoDto;
import site.yesaido.ruleengine_server.registry.dto.threshold.SensorRange;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActuatorControlRuleTest {

    @Mock
    private ActuatorControlStateService actuatorControlStateService;

    @Mock
    private ActuatorCommandExecutor actuatorCommandExecutor;

    private ActuatorControlRule rule;

    private SensorDataDto sensorData;
    private ThresholdInfoDto thresholdInfo;
    private SensorRange sensorRange;

    @BeforeEach
    void setUp() {
        rule = new ActuatorControlRule(1L, actuatorControlStateService, actuatorCommandExecutor);

        sensorData = new SensorDataDto(
                "실습실", "후면 오른쪽",
                "AM107", "AM107-067999", "24e124128c067999",
                "TEMPERATURE",
                BigDecimal.valueOf(23.7),
                OffsetDateTime.now(),
                "°C",
                10L
        );

        sensorRange = new SensorRange("TEMPERATURE", "°C", BigDecimal.valueOf(20.0), BigDecimal.valueOf(30.0));
        thresholdInfo = mock(ThresholdInfoDto.class);
    }

    @Test
    void test_supports_whenValidSensorType_returnsTrue() {
        assertTrue(rule.supports(sensorData));
    }

    @Test
    void test_supports_whenInvalidSensorType_returnsFalse() {
        SensorDataDto soilData = withSensorType(sensorData, "SOIL_PH");

        assertFalse(rule.supports(soilData));
    }

    @Test
    void test_evaluate_whenSensorRangeNotFound_doesNothing() {
        when(thresholdInfo.getRange(sensorData.getSensorType(), sensorData.getUnit())).thenReturn(null);

        rule.evaluate(sensorData, thresholdInfo);

        verifyNoInteractions(actuatorControlStateService);
        verifyNoInteractions(actuatorCommandExecutor);
    }

    @Test
    void test_evaluate_whenPending_doesNothing() {
        when(thresholdInfo.getRange(sensorData.getSensorType(), sensorData.getUnit())).thenReturn(sensorRange);
        when(thresholdInfo.getCultivationId()).thenReturn(10L);
        when(actuatorControlStateService.getState(keyMatching(10L, "TEMPERATURE")))
                .thenReturn(new ActuatorControlState(ActuatorDirection.PENDING, null));

        rule.evaluate(sensorData, thresholdInfo);

        verifyNoInteractions(actuatorCommandExecutor);
        verify(actuatorControlStateService, never()).resetExceededSince(any());
    }

    @Test
    void test_evaluate_whenAlreadyAtTargetDirection_doesNothing() {
        SensorDataDto belowMinData = withValue(sensorData, BigDecimal.valueOf(15.0));
        when(thresholdInfo.getRange(belowMinData.getSensorType(), belowMinData.getUnit())).thenReturn(sensorRange);
        when(thresholdInfo.getCultivationId()).thenReturn(10L);
        when(actuatorControlStateService.getState(keyMatching(10L, "TEMPERATURE")))
                .thenReturn(new ActuatorControlState(ActuatorDirection.INCREASING, OffsetDateTime.now()));

        rule.evaluate(belowMinData, thresholdInfo);

        verifyNoInteractions(actuatorCommandExecutor);
    }

    @Test
    void test_evaluate_whenTargetIsNone_executesImmediatelyWithoutWaiting() {
        SensorDataDto recoveredData = withValue(sensorData, BigDecimal.valueOf(28.0));
        when(thresholdInfo.getRange(recoveredData.getSensorType(), recoveredData.getUnit())).thenReturn(sensorRange);
        when(thresholdInfo.getCultivationId()).thenReturn(10L);
        when(actuatorControlStateService.getState(keyMatching(10L, "TEMPERATURE")))
                .thenReturn(new ActuatorControlState(ActuatorDirection.INCREASING, OffsetDateTime.now()));

        rule.evaluate(recoveredData, thresholdInfo);

        verify(actuatorCommandExecutor, times(1))
                .requestDirectionChange(keyMatching(10L, "TEMPERATURE"), eq(ActuatorDirection.INCREASING), eq(ActuatorDirection.NONE));
    }

    @Test
    void test_evaluate_whenFirstExceeded_recordsWithoutExecuting() {
        SensorDataDto belowMinData = withValue(sensorData, BigDecimal.valueOf(15.0));
        when(thresholdInfo.getRange(belowMinData.getSensorType(), belowMinData.getUnit())).thenReturn(sensorRange);
        when(thresholdInfo.getCultivationId()).thenReturn(10L);
        when(actuatorControlStateService.getState(keyMatching(10L, "TEMPERATURE")))
                .thenReturn(new ActuatorControlState(ActuatorDirection.NONE, null));

        rule.evaluate(belowMinData, thresholdInfo);

        verify(actuatorControlStateService, times(1)).resetExceededSince(keyMatching(10L, "TEMPERATURE"));
        verifyNoInteractions(actuatorCommandExecutor);
    }

    @Test
    void test_evaluate_whenDirectionSwitch_resetsExceededSinceWithoutExecuting() {
        SensorDataDto exceedsMaxData = withValue(sensorData, BigDecimal.valueOf(35.0));
        when(thresholdInfo.getRange(exceedsMaxData.getSensorType(), exceedsMaxData.getUnit())).thenReturn(sensorRange);
        when(thresholdInfo.getCultivationId()).thenReturn(10L);
        when(actuatorControlStateService.getState(keyMatching(10L, "TEMPERATURE")))
                .thenReturn(new ActuatorControlState(ActuatorDirection.INCREASING, OffsetDateTime.now().minusMinutes(5)));

        rule.evaluate(exceedsMaxData, thresholdInfo);

        verify(actuatorControlStateService, times(1)).resetExceededSince(keyMatching(10L, "TEMPERATURE"));
        verifyNoInteractions(actuatorCommandExecutor);
    }

    @Test
    void test_evaluate_whenNotYetSustained_doesNothing() {
        SensorDataDto belowMinData = withValue(sensorData, BigDecimal.valueOf(15.0));
        when(thresholdInfo.getRange(belowMinData.getSensorType(), belowMinData.getUnit())).thenReturn(sensorRange);
        when(thresholdInfo.getCultivationId()).thenReturn(10L);
        when(actuatorControlStateService.getState(keyMatching(10L, "TEMPERATURE")))
                .thenReturn(new ActuatorControlState(ActuatorDirection.NONE, OffsetDateTime.now().minusSeconds(10)));

        rule.evaluate(belowMinData, thresholdInfo);

        verify(actuatorControlStateService, never()).resetExceededSince(any());
        verifyNoInteractions(actuatorCommandExecutor);
    }

    @Test
    void test_evaluate_whenSustainedLongEnough_executesCommand() {
        SensorDataDto belowMinData = withValue(sensorData, BigDecimal.valueOf(15.0));
        when(thresholdInfo.getRange(belowMinData.getSensorType(), belowMinData.getUnit())).thenReturn(sensorRange);
        when(thresholdInfo.getCultivationId()).thenReturn(10L);
        when(actuatorControlStateService.getState(keyMatching(10L, "TEMPERATURE")))
                .thenReturn(new ActuatorControlState(ActuatorDirection.NONE, OffsetDateTime.now().minusMinutes(2)));

        rule.evaluate(belowMinData, thresholdInfo);

        verify(actuatorCommandExecutor, times(1))
                .requestDirectionChange(keyMatching(10L, "TEMPERATURE"), eq(ActuatorDirection.NONE), eq(ActuatorDirection.INCREASING));
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

    private static ActuatorControlKey keyMatching(Long cultivationId, String sensorType) {
        return argThat(k -> k != null
                && cultivationId.equals(k.getCultivationId())
                && sensorType.equals(k.getSensorType()));
    }
}