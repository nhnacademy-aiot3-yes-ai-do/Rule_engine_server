package site.yesaido.ruleengine_server.engine.support;

import com.influxdb.client.write.Point;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import site.yesaido.ruleengine_server.global.dto.SensorValueEvent;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SensorValuePointMapperTest {

    private SensorValuePointMapper mapper;
    private SensorValueEvent event;

    @BeforeEach
    void setUp() {
        mapper = new SensorValuePointMapper();

        event = new SensorValueEvent(
                "실습실", "후면 오른쪽",
                "AM107", "AM107-067999", "24e124128c067999",
                "TEMPERATURE", "°C",
                BigDecimal.valueOf(23.7),
                OffsetDateTime.now(),
                10L
        );
    }

    @Test
    void test_toPoint_withAllFields_mapsCorrectly() {
        Point point = mapper.toPoint(event);

        assertEquals(BigDecimal.valueOf(23.7), point.getFields().get(SensorValuePointMapper.VALUE_FIELD));
        assertEquals("실습실", point.getTags().get("place"));
        assertEquals("후면 오른쪽", point.getTags().get("location"));
        assertEquals("AM107", point.getTags().get("deviceModel"));
        assertEquals("AM107-067999", point.getTags().get("deviceName"));
        assertEquals("24e124128c067999", point.getTags().get("deviceEui"));
        assertEquals("TEMPERATURE", point.getTags().get("sensorType"));
        assertEquals("°C", point.getTags().get("unit"));
        assertEquals("10", point.getTags().get("cultivationId"));
    }

    @Test
    void test_toPoint_whenOptionalTagsBlank_areOmitted() {
        SensorValueEvent eventWithBlankOptionalTags = new SensorValueEvent(
                null, "",
                null, null, "24e124128c067999",
                "TEMPERATURE", "°C",
                BigDecimal.valueOf(23.7),
                OffsetDateTime.now(),
                10L
        );

        Point point = mapper.toPoint(eventWithBlankOptionalTags);

        assertFalse(point.getTags().containsKey("place"));
        assertFalse(point.getTags().containsKey("location"));
        assertFalse(point.getTags().containsKey("deviceModel"));
        assertFalse(point.getTags().containsKey("deviceName"));
        assertTrue(point.getTags().containsKey("deviceEui"));
    }

    @Test
    void test_toPoint_whenEventNull_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> mapper.toPoint(null));
    }

    @Test
    void test_toPoint_whenValueNull_throwsNullPointerException() {
        SensorValueEvent invalid = withValue(null);

        assertThrows(NullPointerException.class, () -> mapper.toPoint(invalid));
    }

    @Test
    void test_toPoint_whenTimeNull_throwsNullPointerException() {
        SensorValueEvent invalid = withTime(null);

        assertThrows(NullPointerException.class, () -> mapper.toPoint(invalid));
    }

    @Test
    void test_toPoint_whenCultivationIdNull_throwsNullPointerException() {
        SensorValueEvent invalid = withCultivationId(null);

        assertThrows(NullPointerException.class, () -> mapper.toPoint(invalid));
    }

    @Test
    void test_toPoint_whenDeviceEuiBlank_throwsIllegalArgumentException() {
        SensorValueEvent invalid = withDeviceEui(" ");

        assertThrows(IllegalArgumentException.class, () -> mapper.toPoint(invalid));
    }

    @Test
    void test_toPoint_whenSensorTypeBlank_throwsIllegalArgumentException() {
        SensorValueEvent invalid = withSensorType("");

        assertThrows(IllegalArgumentException.class, () -> mapper.toPoint(invalid));
    }

    @Test
    void test_toPoint_whenUnitBlank_throwsIllegalArgumentException() {
        SensorValueEvent invalid = withUnit(null);

        assertThrows(IllegalArgumentException.class, () -> mapper.toPoint(invalid));
    }

    // ======================================================================

    private SensorValueEvent withValue(BigDecimal value) {
        return new SensorValueEvent(event.place(), event.location(), event.deviceModel(), event.deviceName(),
                event.deviceEui(), event.sensorType(), event.unit(), value, event.time(), event.cultivationId());
    }

    private SensorValueEvent withTime(OffsetDateTime time) {
        return new SensorValueEvent(event.place(), event.location(), event.deviceModel(), event.deviceName(),
                event.deviceEui(), event.sensorType(), event.unit(), event.value(), time, event.cultivationId());
    }

    private SensorValueEvent withCultivationId(Long cultivationId) {
        return new SensorValueEvent(event.place(), event.location(), event.deviceModel(), event.deviceName(),
                event.deviceEui(), event.sensorType(), event.unit(), event.value(), event.time(), cultivationId);
    }

    private SensorValueEvent withDeviceEui(String deviceEui) {
        return new SensorValueEvent(event.place(), event.location(), event.deviceModel(), event.deviceName(),
                deviceEui, event.sensorType(), event.unit(), event.value(), event.time(), event.cultivationId());
    }

    private SensorValueEvent withSensorType(String sensorType) {
        return new SensorValueEvent(event.place(), event.location(), event.deviceModel(), event.deviceName(),
                event.deviceEui(), sensorType, event.unit(), event.value(), event.time(), event.cultivationId());
    }

    private SensorValueEvent withUnit(String unit) {
        return new SensorValueEvent(event.place(), event.location(), event.deviceModel(), event.deviceName(),
                event.deviceEui(), event.sensorType(), unit, event.value(), event.time(), event.cultivationId());
    }
}
