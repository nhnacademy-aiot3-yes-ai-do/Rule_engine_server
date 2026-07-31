package site.yesaido.ruleengine_server.registry.repository.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import site.yesaido.ruleengine_server.global.dto.SensorType;
import site.yesaido.ruleengine_server.registry.dto.sensor.SensorInfoDto;
import site.yesaido.ruleengine_server.registry.repository.SensorInfoRepository;

import java.util.Optional;

/**
 * SensorInfoRepository의 Redis 기반 구현체입니다.<br>
 * Collector validation 및 RuleEngine 판단 시 필요한 센서 정보를 Redis에 저장 및 조회합니다.
 */
@RequiredArgsConstructor
@Repository
public class SensorInfoRedisRepository implements SensorInfoRepository {

    private static final String KEY_TEMPLATE = "sensor:%s:%s";
    private final RedisTemplate<String, Object> redisTemplate;

    private final ObjectMapper objectMapper;

    @Override
    public void upsertSensorInfo(SensorInfoDto dto) {
        redisTemplate.opsForValue().set(
                buildKey(dto.getDeviceEui(), dto.getSensorType()),
                dto
        );
    }

    @Override
    public Optional<SensorInfoDto> findSensorInfo(String deviceEui, SensorType sensorType) {
        Object rawValue = redisTemplate.opsForValue().get(buildKey(deviceEui, sensorType));

        if (rawValue == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(
                objectMapper.convertValue(rawValue, SensorInfoDto.class)
        );
    }

    @Override
    public void deleteSensorInfo(String deviceEui, SensorType sensorType) {
        redisTemplate.delete(
                buildKey(deviceEui, sensorType)
        );
    }

    @Override
    public boolean exists(String deviceEui, SensorType sensorType) {
        return Boolean.TRUE.equals(
                redisTemplate.hasKey(buildKey(deviceEui, sensorType))
        );
    }

    // ==================================================

    private String buildKey(String deviceEui, SensorType sensorType) {
        return KEY_TEMPLATE.formatted(deviceEui, sensorType.name());
    }
}
