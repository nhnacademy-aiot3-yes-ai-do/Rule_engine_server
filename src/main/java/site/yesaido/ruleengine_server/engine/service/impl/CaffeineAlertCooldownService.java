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
    private final Cache<SensorKey, Boolean> stateMap;

    public CaffeineAlertCooldownService(@Value("${rule-engine.alert.cooldown.minutes}") long alertCooldown,
                                        @Value("${rule-engine.alert.state.ttl-hours}") long stateTtlHours) {
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(alertCooldown))
                .build();
        this.stateMap = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofHours(stateTtlHours))
                .build();
    }

    @Override
    public boolean tryMarkExceeded(SensorKey sensorKey) {

        stateMap.put(sensorKey, true);

        return cache.asMap().putIfAbsent(sensorKey, true) == null;
    }

    @Override
    public boolean tryMarkNormal(SensorKey sensorKey) {

        Boolean wasExceeded = stateMap.asMap().remove(sensorKey);
        if(Boolean.TRUE.equals(wasExceeded)) {
            cache.invalidate(sensorKey);
            return true;
        }
        return false;
    }
}
