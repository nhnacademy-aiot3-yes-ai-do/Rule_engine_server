package site.yesaido.ruleengine_server.global.util;

public class SensorTypeUtils {

    private SensorTypeUtils() {}

    public static String normalize(String sensorType) {
        if(sensorType == null) {
            return null;
        }

        return sensorType.trim().toUpperCase();
    }
}
