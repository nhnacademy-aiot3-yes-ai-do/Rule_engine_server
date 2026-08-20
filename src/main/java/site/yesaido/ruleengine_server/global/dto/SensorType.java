package site.yesaido.ruleengine_server.global.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 시스템에서 기본적으로 지원하는 센서 데이터의 종류를 정의하는 Enum입니다.
 */
@Getter
@RequiredArgsConstructor
public enum SensorType {

    /**
     * 온도 센서
     */
    TEMPERATURE("온도"),

    /**
     * 습도 센서
     */
    HUMIDITY("습도"),

    /**
     * 이산화탄소 센서
     */
    CO2("이산화탄소"),

    /**
     * 조도 센서
     */
    LIGHT("조도");

    /**
     * 센서 타입의 한글 표기명
     */
    private final String koreanName;

    /**
     * 대소문자 구분 없이 문자열을 매칭하여 해당하는 {@link SensorType} 상수를 반환합니다.
     *
     * @param value 센서 타입 문자열
     * @return 일치하는 SensorType 상수 (null 입력 시 null)
     * @throws IllegalArgumentException 정의되지 않은 센서 타입인 경우
     */
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
