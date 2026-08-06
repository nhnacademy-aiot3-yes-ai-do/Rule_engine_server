package site.yesaido.ruleengine_server.registry.repository.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import site.yesaido.ruleengine_server.registry.dto.cultivation.CultivationInfoDto;
import site.yesaido.ruleengine_server.registry.repository.CultivationInfoRepository;

import java.util.Optional;

/**
 * CultivationInfoRepository의 Redis 기반 구현체입니다.<br>
 * Collector validation 및 RuleEngine 판단 시 필요한 재배 환경 정보를 Redis에 저장 및 조회합니다.
 */
@RequiredArgsConstructor
@Repository
public class CultivationInfoRedisRepository implements CultivationInfoRepository {

    private static final String KEY_TEMPLATE = "cultivation:%d";
    private final RedisTemplate<String, Object> redisTemplate;

    private final ObjectMapper objectMapper;

    @Override
    public void upsertCultivationInfo(CultivationInfoDto dto) {
        redisTemplate.opsForValue().set(
                buildKey(dto.getCultivationId()),
                dto
        );
    }

    @Override
    public Optional<CultivationInfoDto> findCultivationInfo(Long cultivationId) {
        Object rawValue = redisTemplate.opsForValue().get(buildKey(cultivationId));

        if (rawValue == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(
                objectMapper.convertValue(rawValue, CultivationInfoDto.class)
        );
    }

    @Override
    public void deleteCultivationInfo(Long cultivationId) {
        redisTemplate.delete(
                buildKey(cultivationId)
        );
    }

    @Override
    public boolean exists(Long cultivationId) {
        return Boolean.TRUE.equals(
                redisTemplate.hasKey(buildKey(cultivationId))
        );
    }

    // ==================================================

    private String buildKey(Long cultivationId) {
        return KEY_TEMPLATE.formatted(cultivationId);
    }
}
