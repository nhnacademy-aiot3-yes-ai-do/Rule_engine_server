package site.yesaido.ruleengine_server.engine.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import site.yesaido.ruleengine_server.engine.dto.SensorKey;
import site.yesaido.ruleengine_server.engine.service.AlertCooldownService;

import java.time.Duration;

@Service
public class CaffeineAlertCooldownService implements AlertCooldownService {

    private final Cache<SensorKey, Boolean> cache;

    @Value("${rule-engine.alert.cooldown.minutes}")
    private long alertCooldown;

    public CaffeineAlertCooldownService() {
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
    public void resetCooldown(SensorKey sensorKey) {
        cache.invalidate(sensorKey);
    }
}
