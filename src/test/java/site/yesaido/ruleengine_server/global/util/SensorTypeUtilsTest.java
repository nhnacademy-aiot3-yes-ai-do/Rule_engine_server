package site.yesaido.ruleengine_server.global.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SensorTypeUtilsTest {

    @Test
    void test_normalize_trimsAndUppercases() {
        Assertions.assertEquals("TEMPERATURE", SensorTypeUtils.normalize("  temperature  "));
    }

    @Test
    void test_normalize_alreadyNormalized() {
        Assertions.assertEquals("TEMPERATURE", SensorTypeUtils.normalize("TEMPERATURE"));
    }

    @Test
    void test_normalize_mixedCase() {
        Assertions.assertEquals("CO2", SensorTypeUtils.normalize("Co2"));
    }

    @Test
    void test_normalize_nullReturnsNull() {
        Assertions.assertNull(SensorTypeUtils.normalize(null));
    }

    @Test
    void test_normalize_emptyString() {
        Assertions.assertEquals("", SensorTypeUtils.normalize("   "));
    }
}