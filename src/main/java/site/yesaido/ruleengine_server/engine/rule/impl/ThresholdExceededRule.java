package site.yesaido.ruleengine_server.engine.rule.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import site.yesaido.ruleengine_server.collector.dto.SensorDataDto;
import site.yesaido.ruleengine_server.engine.dto.SensorKey;
import site.yesaido.ruleengine_server.engine.rule.Rule;
import site.yesaido.ruleengine_server.engine.service.AlertCooldownService;
import site.yesaido.ruleengine_server.engine.service.NotificationService;
import site.yesaido.ruleengine_server.global.dto.ThresholdInfoDto;

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
        ThresholdInfoDto.Range range = thresholdInfo.getRange(sensorData.getSensorType());
        if (range == null) {
            return;
        }

        BigDecimal sensorValue = sensorData.getValue();
        BigDecimal thresholdMin = range.getMin();
        BigDecimal thresholdMax = range.getMax();

        boolean exceeded = sensorValue.compareTo(thresholdMin) < 0 || sensorValue.compareTo(thresholdMax) > 0;

        SensorKey sensorKey = new SensorKey(
                sensorData.getDeviceEui(),
                sensorData.getSensorType(),
                sensorData.getUnit()
        );

        if (exceeded) {
            if (!alertCooldownService.canAlert(sensorKey)) {
                return;
            }

            notificationService.sendAlert();
            alertCooldownService.recordAlert(sensorKey);
        }

        alertCooldownService.resetCooldown(sensorKey);
    }
}
