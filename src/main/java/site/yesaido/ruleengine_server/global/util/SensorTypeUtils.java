package site.yesaido.ruleengine_server.global.util;

/**
 * 센서 타입 문자열 처리를 위한 유틸리티 클래스입니다.
 */
public class SensorTypeUtils {

    private SensorTypeUtils() {}

    /**
     * 센서 타입 문자열의 앞뒤 공백을 제거하고 대문자로 정규화합니다.
     *
     * @param sensorType 정규화할 센서 타입 문자열
     * @return 대문자로 정규화된 문자열 (null 입력 시 null 반환)
     */
    public static String normalize(String sensorType) {
        if(sensorType == null) {
            return null;
        }

        return sensorType.trim().toUpperCase();
    }
}
