package site.yesaido.ruleengine_server.engine.rule.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import site.yesaido.ruleengine_server.collector.dto.SensorDataDto;
import site.yesaido.ruleengine_server.engine.dto.SensorKey;
import site.yesaido.ruleengine_server.engine.rule.Rule;
import site.yesaido.ruleengine_server.engine.service.AlertCooldownService;
import site.yesaido.ruleengine_server.engine.service.NotificationService;
import site.yesaido.ruleengine_server.global.dto.ThresholdInfoDto;
import site.yesaido.ruleengine_server.registry.dto.threshold.SensorRange;

import java.math.BigDecimal;

@RequiredArgsConstructor
@Component
public class ThresholdExceededRule implements Rule {

    private final NotificationService notificationService;
    private final AlertCooldownService alertCooldownService;

    @Override
    public boolean supports(SensorDataDto dto) {
        // 조건 검사 없이, 모든 센서 데이터에 대해 적용
        return true;
    }

    @Override
    public void evaluate(SensorDataDto sensorData, ThresholdInfoDto thresholdInfo) {
        SensorRange sensorRange = thresholdInfo.getRange(sensorData.getSensorType(), sensorData.getUnit());
        if (sensorRange == null) {
            return;
        }

        BigDecimal sensorValue = sensorData.getValue();
        BigDecimal thresholdMin = sensorRange.getMinValue();
        BigDecimal thresholdMax = sensorRange.getMaxValue();

        boolean exceeded = sensorValue.compareTo(thresholdMin) < 0 || sensorValue.compareTo(thresholdMax) > 0;

        SensorKey sensorKey = new SensorKey(
                sensorData.getDeviceEui(),
                sensorData.getSensorType(),
                sensorData.getUnit()
        );

        if (exceeded) {
            if (alertCooldownService.canAlert(sensorKey)) {
                notificationService.sendThresholdExceededAlert(sensorData); // 초과 알림
                alertCooldownService.recordAlert(sensorKey);
            }

            alertCooldownService.markExceeded(sensorKey);

            return;
        }

        if(alertCooldownService.isCurrentlyExceeded(sensorKey)) {
            notificationService.sendThresholdRecoveredAlert(sensorData); // 복귀 알림
            alertCooldownService.markNormal(sensorKey);
        }
    }
}
