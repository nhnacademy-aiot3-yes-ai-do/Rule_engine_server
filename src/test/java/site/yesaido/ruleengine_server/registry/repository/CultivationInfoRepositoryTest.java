package site.yesaido.ruleengine_server.registry.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import site.yesaido.ruleengine_server.registry.dto.threshold.ThresholdInfoEvent;
import site.yesaido.ruleengine_server.registry.repository.impl.CultivationInfoRedisRepository;

import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CultivationInfoRepositoryTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private CultivationInfoRedisRepository repository;

    private ThresholdInfoEvent dto;

    @BeforeEach
    void setUp() {
        dto = new ThresholdInfoEvent(
                1L,
                20.0, 30.0,
                60.0, 80.0,
                600.0, 800.0,
                0.0, 500.0
        );
    }

    @Test
    void test_upsertCultivationInfo() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        repository.upsertCultivationInfo(dto);

        verify(valueOperations, times(1)).set(anyString(), any(ThresholdInfoEvent.class));
    }

    @Test
    void test_deleteCultivationInfo() {
        repository.deleteCultivationInfo(1L);

        verify(redisTemplate, times(1)).delete(anyString());
    }

    @Test
    void test_findCultivationInfo_success() {
        Object object = new Object();
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(object);
        when(objectMapper.convertValue(object, ThresholdInfoEvent.class)).thenReturn(dto);

        Optional<ThresholdInfoEvent> result = repository.findCultivationInfo(dto.getCultivationId());

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(dto, result.get());
        verify(valueOperations, times(1)).get(anyString());
    }

    @Test
    void test_findCultivationInfo_fail() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);

        Optional<ThresholdInfoEvent> result = repository.findCultivationInfo(dto.getCultivationId());

        Assertions.assertTrue(result.isEmpty());
        verify(objectMapper, never()).convertValue(any(), eq(ThresholdInfoEvent.class));
    }

    @Test
    void test_exists_returnTrue() {
        when(redisTemplate.hasKey(anyString())).thenReturn(true);

        Assertions.assertTrue(repository.exists(1L));
        verify(redisTemplate, times(1)).hasKey(anyString());
    }

    @Test
    void test_exists_returnFalse() {
        when(redisTemplate.hasKey(anyString())).thenReturn(false);

        Assertions.assertFalse(repository.exists(1L));
        verify(redisTemplate, times(1)).hasKey(anyString());
    }

}
