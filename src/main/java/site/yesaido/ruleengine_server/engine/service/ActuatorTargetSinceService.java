package site.yesaido.ruleengine_server.engine.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;
import site.yesaido.ruleengine_server.engine.dto.actuator.ActuatorControlKey;
import site.yesaido.ruleengine_server.engine.dto.actuator.ActuatorType;

import java.time.Instant;

@Service
public class ActuatorTargetSinceService {

    private final Cache<ActuatorControlKey, TargetSince> targetSinceCache = Caffeine.newBuilder().build();

    public record TargetSince(ActuatorType target, Instant since) {}

    public TargetSince getTargetSince(ActuatorControlKey key) {
        return targetSinceCache.getIfPresent(key);
    }

    public void setTargetSince(ActuatorControlKey key, TargetSince targetSince) {
        targetSinceCache.put(key, targetSince);
    }

    public void clear(ActuatorControlKey key) {
        targetSinceCache.invalidate(key);
    }
}
