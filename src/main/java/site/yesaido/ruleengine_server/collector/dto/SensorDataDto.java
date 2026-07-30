package site.yesaido.ruleengine_server.collector.dto;

import lombok.*;
import site.yesaido.ruleengine_server.global.dto.SensorType;

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

    private String deviceName;

    private String deviceEui;

    private SensorType sensorType;

    private Double value;
}
