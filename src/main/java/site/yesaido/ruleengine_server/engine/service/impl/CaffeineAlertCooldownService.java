package site.yesaido.ruleengine_server.engine.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import site.yesaido.ruleengine_server.engine.dto.SensorKey;
import site.yesaido.ruleengine_server.engine.service.AlertCooldownService;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CaffeineAlertCooldownService implements AlertCooldownService {

    private final Cache<SensorKey, Boolean> cache;
    private final Map<SensorKey, Boolean> stateMap = new ConcurrentHashMap<>();

    public CaffeineAlertCooldownService(@Value("${rule-engine.alert.cooldown.minutes}") long alertCooldown) {
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(alertCooldown))
                .build();
    }

    @Override
    public boolean canAlert(SensorKey sensorKey) {
        return cache.getIfPresent(sensorKey) == null;
    }

    @Override
    public void recordAlert(SensorKey sensorKey) {
        cache.put(sensorKey, true);
    }

    @Override
    public boolean isCurrentlyExceeded(SensorKey sensorKey) {
        return Boolean.TRUE.equals(stateMap.get(sensorKey));
    }

    @Override
    public void markExceeded(SensorKey sensorKey) {
        stateMap.put(sensorKey, true);
    }

    @Override
    public void markNormal(SensorKey sensorKey) {
        stateMap.remove(sensorKey);
    }

}
