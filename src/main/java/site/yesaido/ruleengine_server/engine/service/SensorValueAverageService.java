package site.yesaido.ruleengine_server.engine.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import site.yesaido.ruleengine_server.engine.dto.actuator.SensorValueKey;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class SensorValueAverageService {

    private final Cache<SensorValueKey, BigDecimal> valueCache;

    public SensorValueAverageService(@Value("${rule-engine.actuator.sensor-value.stale-after-seconds}") long staleAfterSeconds) {
        this.valueCache = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofSeconds(staleAfterSeconds))
                .build();
    }

    public void put(SensorValueKey key, BigDecimal value) {
        valueCache.put(key, value);
    }

    public BigDecimal getAverage(Long cultivationId, String sensorType) {

        List<BigDecimal> values = valueCache.asMap().entrySet().stream()
                .filter(entry -> entry.getKey().cultivationId().equals(cultivationId) && entry.getKey().sensorType().equals(sensorType))
                .map(Map.Entry::getValue)
                .toList();

        if (values.isEmpty()) {
            return null;
        }

        BigDecimal sum = values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(values.size()), 2, RoundingMode.HALF_UP);
    }
}
