package site.yesaido.ruleengine_server.engine.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import site.yesaido.ruleengine_server.engine.dto.actuator.ActuatorControlKey;
import site.yesaido.ruleengine_server.engine.dto.actuator.ActuatorControlState;
import site.yesaido.ruleengine_server.engine.repository.ActuatorControlStateRepository;
import tools.jackson.databind.ObjectMapper;

import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class ActuatorControlStateRedisRepository implements ActuatorControlStateRepository {

    private static final String KEY_TEMPLATE = "actuator-control:%d:%s";

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public Optional<ActuatorControlState> find(ActuatorControlKey key) {

        Object rawValue = redisTemplate.opsForValue().get(buildKey(key));
        if (rawValue == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(
                objectMapper.convertValue(rawValue, ActuatorControlState.class)
        );
    }

    @Override
    public void save(ActuatorControlKey key, ActuatorControlState state) {

        redisTemplate.opsForValue().set(buildKey(key), state);
    }

    // ======================================================================

    private String buildKey(ActuatorControlKey key) {

        return KEY_TEMPLATE.formatted(key.getCultivationId(), key.getSensorType());
    }
}
