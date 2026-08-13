package site.yesaido.ruleengine_server.global.dto;

import site.yesaido.ruleengine_server.collector.dto.SensorDataDto;


import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record SensorValueEvent(
        String place,
        String location,
        String deviceModel,
        String deviceName,
        String deviceEui,
        String sensorType,
        String unit,
        BigDecimal value,
        OffsetDateTime time,
        Long cultivationId
) {
    public static SensorValueEvent from(SensorDataDto dto) {
        return new SensorValueEvent(
                dto.getPlace(),
                dto.getLocation(),
                dto.getDeviceModel(),
                dto.getDeviceName(),
                dto.getDeviceEui(),
                dto.getSensorType(),
                dto.getUnit(),
                dto.getValue(),
                dto.getTime(),
                dto.getCultivationId()
        );
    }
}