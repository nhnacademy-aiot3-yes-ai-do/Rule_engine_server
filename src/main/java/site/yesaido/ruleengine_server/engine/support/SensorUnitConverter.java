package site.yesaido.ruleengine_server.engine.support;

import java.math.BigDecimal;

public class SensorUnitConverter {

    private SensorUnitConverter() {}

    public static BigDecimal convert(BigDecimal value, String fromUnit, String toUnit) {

        if (fromUnit.equals(toUnit)) {
            return value;
        }

        double doubleValue = value.doubleValue();

        if (fromUnit.equals("°F") && toUnit.equals("°C")) {
            return BigDecimal.valueOf((doubleValue - 32) * 5 / 9);
        }

        if (fromUnit.equals("°C") && toUnit.equals("°F")) {
            return BigDecimal.valueOf(doubleValue * 9 / 5 + 32);
        }

        throw new IllegalArgumentException("지원하지 않는 단위 변환: %s -> %s".formatted(fromUnit, toUnit));
    }
}
