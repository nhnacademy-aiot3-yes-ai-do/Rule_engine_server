package site.yesaido.ruleengine_server.engine.support;

import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import site.yesaido.ruleengine_server.global.dto.SensorValueEvent;

import java.util.Objects;

/**
 * {@link SensorValueEvent}를 InfluxDB 저장을 위한 {@link Point} 객체로 변환하는 매퍼 클래스입니다.
 */
public class SensorValuePointMapper {

    /**
     * InfluxDB Measurement 이름
     */
    public static final String MEASUREMENT = "sensor_value";

    /**
     * 센서 측정값 필드 키
     */
    public static final String VALUE_FIELD = "value";

    /**
     * {@link SensorValueEvent}를 InfluxDB {@link Point} 객체로 변환합니다.
     *
     * @param event 변환할 센서 측정값 이벤트 객체
     * @return InfluxDB 저장을 위한 Point 객체
     * @throws NullPointerException 필수 필드(event, value, time, cultivationId)가 null인 경우
     * @throws IllegalArgumentException 필수 문자열 필드(deviceEui, sensorType, unit)가 비어있는 경우
     */
    public Point toPoint(SensorValueEvent event) {
        Objects.requireNonNull(event, "event must not be null");
        Objects.requireNonNull(event.value(), "event.value must not be null");
        Objects.requireNonNull(event.time(), "event.time must not be null");
        Objects.requireNonNull(event.cultivationId(), "event.cultivationId must not be null");
        requireText(event.deviceEui(), "event.deviceEui must not be blank");
        requireText(event.sensorType(), "event.sensorType must not be null");
        requireText(event.unit(), "event.unit must not be null");

        Point point = Point.measurement(MEASUREMENT)
                .addField(VALUE_FIELD, event.value())
                .time(event.time().toInstant(), WritePrecision.MS);

        addTag(point, "place", event.place());
        addTag(point, "location", event.location());
        addTag(point, "deviceModel", event.deviceModel());
        addTag(point, "deviceName", event.deviceName());
        addTag(point, "deviceEui", event.deviceEui());
        addTag(point, "sensorType", event.sensorType());
        addTag(point, "unit", event.unit());
        addTag(point, "cultivationId", event.cultivationId().toString());

        return point;
    }

    /**
     * 태그 값이 존재하는 경우에만 Point에 태그를 추가합니다.
     *
     * @param point InfluxDB Point
     * @param key   태그 키
     * @param value 태그 값
     */
    private void addTag(Point point, String key, String value) {
        if (value != null && !value.isBlank()) {
            point.addTag(key, value);
        }
    }

    /**
     * 문자열이 null이거나 공백인지 검증합니다.
     *
     * @param value   검증할 문자열
     * @param message 예외 메시지
     * @throws IllegalArgumentException 문자열이 null이거나 공백인 경우
     */
    private void requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }
}