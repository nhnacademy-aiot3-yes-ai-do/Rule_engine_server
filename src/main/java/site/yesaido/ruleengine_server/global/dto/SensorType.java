package site.yesaido.ruleengine_server.global.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SensorType {

    // 센서 데이터 종류를 Enum을 통해 관리합니다.

    TEMPERATURE("온도"),
    HUMIDITY("습도"),
    CO2("이산화탄소"),
    LIGHT("조도");

    private final String koreanName;

    @JsonCreator
    public static SensorType from(String value) {
        if (value == null) {
            return null;
        }
        for (SensorType sensorType : SensorType.values()) {
            if (sensorType.name().equalsIgnoreCase(value)) {
                return sensorType;
            }
        }
        throw new IllegalArgumentException("Unknown SensorType: " + value);
    }
}
