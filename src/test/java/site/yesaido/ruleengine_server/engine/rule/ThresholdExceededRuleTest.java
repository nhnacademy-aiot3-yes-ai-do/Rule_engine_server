package site.yesaido.ruleengine_server.engine.rule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.yesaido.ruleengine_server.collector.dto.SensorDataDto;
import site.yesaido.ruleengine_server.engine.dto.SensorKey;
import site.yesaido.ruleengine_server.engine.rule.impl.ThresholdExceededRule;
import site.yesaido.ruleengine_server.engine.service.AlertCooldownService;
import site.yesaido.ruleengine_server.engine.service.NotificationService;
import site.yesaido.ruleengine_server.global.dto.ThresholdInfoDto;
import site.yesaido.ruleengine_server.registry.dto.threshold.SensorRange;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ThresholdExceededRuleTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private AlertCooldownService alertCooldownService;

    @InjectMocks
    private ThresholdExceededRule rule;

    private SensorDataDto sensorData;
    private ThresholdInfoDto thresholdInfo;
    private SensorRange sensorRange;
    private SensorKey sensorKey;

    @BeforeEach
    void setUp() {
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

        sensorKey = new SensorKey(
                sensorData.getDeviceEui(),
                sensorData.getSensorType(),
                sensorData.getUnit()
        );
    }

    @Test
    void test_supports_alwaysTrue() {
        assertTrue(rule.supports(sensorData));
    }

    @Test
    void test_evaluate_whenSensorRangeNotFound_doesNothing() {
        when(thresholdInfo.getRange(sensorData.getSensorType(), sensorData.getUnit())).thenReturn(null);

        rule.evaluate(sensorData, thresholdInfo);

        verifyNoInteractions(notificationService);
        verifyNoInteractions(alertCooldownService);
    }

    @Test
    void test_evaluate_whenExceededAndTryMarkExceededReturnsTrue_sendsExceededAlert() {
        SensorDataDto exceededData = withValue(BigDecimal.valueOf(35.0));
        when(thresholdInfo.getRange(exceededData.getSensorType(), exceededData.getUnit())).thenReturn(sensorRange);
        when(alertCooldownService.tryMarkExceeded(sensorKey)).thenReturn(true);

        rule.evaluate(exceededData, thresholdInfo);

        verify(notificationService, times(1)).sendThresholdExceededAlert(exceededData);
        verify(notificationService, never()).sendThresholdRecoveredAlert(any());
    }

    @Test
    void test_evaluate_whenExceededAndTryMarkExceededReturnsFalse_doesNotSendAlert() {
        SensorDataDto exceededData = withValue(BigDecimal.valueOf(35.0));
        when(thresholdInfo.getRange(exceededData.getSensorType(), exceededData.getUnit())).thenReturn(sensorRange);
        when(alertCooldownService.tryMarkExceeded(sensorKey)).thenReturn(false);

        rule.evaluate(exceededData, thresholdInfo);

        verify(notificationService, never()).sendThresholdExceededAlert(any());
    }

    @Test
    void test_evaluate_whenBelowMin_isTreatedAsExceeded() {
        SensorDataDto belowMinData = withValue(BigDecimal.valueOf(10.0));
        when(thresholdInfo.getRange(belowMinData.getSensorType(), belowMinData.getUnit())).thenReturn(sensorRange);
        when(alertCooldownService.tryMarkExceeded(sensorKey)).thenReturn(true);

        rule.evaluate(belowMinData, thresholdInfo);

        verify(notificationService, times(1)).sendThresholdExceededAlert(belowMinData);
        verify(alertCooldownService, never()).tryMarkNormal(any());
    }

    @Test
    void test_evaluate_whenNormalAndTryMarkNormalReturnsTrue_sendsRecoveredAlert() {
        SensorDataDto normalData = withValue(BigDecimal.valueOf(25.0));
        when(thresholdInfo.getRange(normalData.getSensorType(), normalData.getUnit())).thenReturn(sensorRange);
        when(alertCooldownService.tryMarkNormal(sensorKey)).thenReturn(true);

        rule.evaluate(normalData, thresholdInfo);

        verify(notificationService, times(1)).sendThresholdRecoveredAlert(normalData);
        verify(notificationService, never()).sendThresholdExceededAlert(any());
        verify(alertCooldownService, never()).tryMarkExceeded(any());
    }

    @Test
    void test_evaluate_whenNormalAndTryMarkNormalReturnsFalse_doesNothing() {
        SensorDataDto normalData = withValue(BigDecimal.valueOf(25.0));
        when(thresholdInfo.getRange(normalData.getSensorType(), normalData.getUnit())).thenReturn(sensorRange);
        when(alertCooldownService.tryMarkNormal(sensorKey)).thenReturn(false);

        rule.evaluate(normalData, thresholdInfo);

        verify(notificationService, never()).sendThresholdRecoveredAlert(any());
    }

    // ======================================================================

    private SensorDataDto withValue(BigDecimal value) {
        return new SensorDataDto(
                sensorData.getPlace(), sensorData.getLocation(),
                sensorData.getDeviceModel(), sensorData.getDeviceName(), sensorData.getDeviceEui(),
                sensorData.getSensorType(),
                value,
                sensorData.getTime(),
                sensorData.getUnit(),
                sensorData.getCultivationId()
        );
    }
}