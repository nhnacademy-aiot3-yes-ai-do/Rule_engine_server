package site.yesaido.ruleengine_server.engine.service;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import site.yesaido.ruleengine_server.engine.dto.actuator.SensorValueKey;

import java.math.BigDecimal;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SensorValueAverageServiceTest {

    private static final Long CULTIVATION_ID = 1L;
    private static final String SENSOR_TYPE = "TEMPERATURE";

    @Test
    void test_getAverage_whenNoValues_returnsNull() {
        SensorValueAverageService service = new SensorValueAverageService(20);

        BigDecimal average = service.getAverage(CULTIVATION_ID, SENSOR_TYPE);

        assertNull(average);
    }

    @Test
    void test_getAverage_singleValue_returnsThatValue() {
        SensorValueAverageService service = new SensorValueAverageService(20);

        service.put(new SensorValueKey(CULTIVATION_ID, SENSOR_TYPE, "device-A"), BigDecimal.valueOf(23.7));

        BigDecimal average = service.getAverage(CULTIVATION_ID, SENSOR_TYPE);

        assertTrue(BigDecimal.valueOf(23.7).compareTo(average) == 0);
    }

    @Test
    void test_getAverage_multipleValues_returnsAverage() {
        SensorValueAverageService service = new SensorValueAverageService(20);

        service.put(new SensorValueKey(CULTIVATION_ID, SENSOR_TYPE, "device-A"), BigDecimal.valueOf(30));
        service.put(new SensorValueKey(CULTIVATION_ID, SENSOR_TYPE, "device-B"), BigDecimal.valueOf(32));

        BigDecimal average = service.getAverage(CULTIVATION_ID, SENSOR_TYPE);

        assertTrue(BigDecimal.valueOf(31.0).compareTo(average) == 0);
    }

    @Test
    void test_getAverage_samplesUpdatedByLatestPutPerDevice() {
        SensorValueAverageService service = new SensorValueAverageService(20);
        SensorValueKey deviceA = new SensorValueKey(CULTIVATION_ID, SENSOR_TYPE, "device-A");

        service.put(deviceA, BigDecimal.valueOf(20));
        service.put(deviceA, BigDecimal.valueOf(40));

        BigDecimal average = service.getAverage(CULTIVATION_ID, SENSOR_TYPE);

        assertTrue(BigDecimal.valueOf(40.0).compareTo(average) == 0);
    }

    @Test
    void test_getAverage_isolatedByCultivationIdAndSensorType() {
        SensorValueAverageService service = new SensorValueAverageService(20);

        service.put(new SensorValueKey(CULTIVATION_ID, SENSOR_TYPE, "device-A"), BigDecimal.valueOf(30));
        service.put(new SensorValueKey(2L, SENSOR_TYPE, "device-B"), BigDecimal.valueOf(999));
        service.put(new SensorValueKey(CULTIVATION_ID, "HUMIDITY", "device-C"), BigDecimal.valueOf(999));

        BigDecimal average = service.getAverage(CULTIVATION_ID, SENSOR_TYPE);

        assertTrue(BigDecimal.valueOf(30.0).compareTo(average) == 0);
    }

    @Test
    void test_getAverage_excludesStaleValues() {
        SensorValueAverageService service = new SensorValueAverageService(0);

        service.put(new SensorValueKey(CULTIVATION_ID, SENSOR_TYPE, "device-A"), BigDecimal.valueOf(30));

        Awaitility.await()
                .atMost(Duration.ofSeconds(2))
                .untilAsserted(() -> assertNull(service.getAverage(CULTIVATION_ID, SENSOR_TYPE)));
    }
}
