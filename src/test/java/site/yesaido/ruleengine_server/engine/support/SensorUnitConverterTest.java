package site.yesaido.ruleengine_server.engine.support;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SensorUnitConverterTest {

    @Test
    void test_convert_whenSameUnit_returnsUnchangedValue() {
        BigDecimal value = BigDecimal.valueOf(23.7);

        BigDecimal result = SensorUnitConverter.convert(value, "°C", "°C");

        assertEquals(0, value.compareTo(result));
    }

    @Test
    void test_convert_fahrenheitToCelsius_freezingPoint() {
        BigDecimal result = SensorUnitConverter.convert(BigDecimal.valueOf(32), "°F", "°C");

        assertEquals(0, BigDecimal.valueOf(0.0).compareTo(result));
    }

    @Test
    void test_convert_fahrenheitToCelsius_boilingPoint() {
        BigDecimal result = SensorUnitConverter.convert(BigDecimal.valueOf(212), "°F", "°C");

        assertEquals(0, BigDecimal.valueOf(100.0).compareTo(result));
    }

    @Test
    void test_convert_celsiusToFahrenheit_freezingPoint() {
        BigDecimal result = SensorUnitConverter.convert(BigDecimal.valueOf(0), "°C", "°F");

        assertEquals(0, BigDecimal.valueOf(32.0).compareTo(result));
    }

    @Test
    void test_convert_celsiusToFahrenheit_boilingPoint() {
        BigDecimal result = SensorUnitConverter.convert(BigDecimal.valueOf(100), "°C", "°F");

        assertEquals(0, BigDecimal.valueOf(212.0).compareTo(result));
    }

    @Test
    void test_convert_whenUnsupportedUnitPair_throwsIllegalArgumentException() {
        BigDecimal value = BigDecimal.valueOf(50);

        assertThrows(IllegalArgumentException.class, () -> SensorUnitConverter.convert(value, "%", "ppm"));
    }
}
