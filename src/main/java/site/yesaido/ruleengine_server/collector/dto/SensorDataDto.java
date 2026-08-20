package site.yesaido.ruleengine_server.collector.dto;

import lombok.*;
import site.yesaido.ruleengine_server.global.dto.SensorType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

/**
 * 센서로부터 수집한 데이터를 담고 있는 DTO입니다.<br>
 * MQTT 구독을 통해 수신한 메세지에서 필요한 값만 추출하여 사용합니다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SensorDataDto {

    private String place;

    private String location;

    private String deviceModel;

    private String deviceName;

    private String deviceEui;

    private String sensorType;

    private BigDecimal value;

    private OffsetDateTime time;

    private String unit;

    private Long cultivationId;

    public SensorDataDto(String place, String location,
                         String deviceModel, String deviceName, String deviceEui,
                         String sensorType,
                         BigDecimal value, OffsetDateTime time, String unit) {
        this(place, location, deviceModel, deviceName, deviceEui, sensorType, value, time, unit, null);
    }
}
