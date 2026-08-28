package site.yesaido.ruleengine_server.registry.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import site.yesaido.ruleengine_server.registry.repository.ManagedSensorTypeRepository;

import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Repository
public class ManagedSensorTypeRedisRepository implements ManagedSensorTypeRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String KEY = "managed-sensor-types";

    @Override
    public void register(String sensorType) {
        // Set에 이미 존재하는 원소이면 아무 동작을 하지 않고 리턴됨
        redisTemplate.opsForSet().add(KEY, sensorType);
    }

    @Override
    public boolean isManaged(String sensorType) {
        return Boolean.TRUE.equals(
                redisTemplate.opsForSet().isMember(KEY, sensorType)
        );
    }

    @Override
    public Set<String> findAll() {
        Set<Object> members = redisTemplate.opsForSet().members(KEY);

        return members.stream()
                .map(String::valueOf)
                .collect(Collectors.toSet());
    }
}
