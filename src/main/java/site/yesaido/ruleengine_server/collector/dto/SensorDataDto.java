package site.yesaido.ruleengine_server.collector.dto;

import lombok.*;
import site.yesaido.ruleengine_server.global.dto.SensorType;

import java.time.LocalDateTime;

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

    private SensorType sensorType;

    private Double value;

    private LocalDateTime time;

    private String unit;

    private Long cultivationId;

    public SensorDataDto(String place, String location,
                         String deviceModel, String deviceName, String deviceEui,
                         SensorType sensorType,
                         Double value, LocalDateTime time, String unit) {
        this(place, location, deviceModel, deviceName, deviceEui, sensorType, value, time, unit, null);
    }
}
