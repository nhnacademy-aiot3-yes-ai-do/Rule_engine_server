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
import site.yesaido.ruleengine_server.global.dto.ThresholdInfoDto;
import site.yesaido.ruleengine_server.registry.dto.threshold.ThresholdInfoEvent;
import site.yesaido.ruleengine_server.registry.repository.impl.ThresholdInfoRedisRepository;

import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ThresholdInfoRepositoryTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ThresholdInfoRedisRepository repository;

    private ThresholdInfoDto dto;

    @BeforeEach
    void setUp() {
        dto = new ThresholdInfoDto(
                1L,
                20.0, 30.0,
                60.0, 80.0,
                600.0, 800.0,
                0.0, 500.0
        );
    }

    @Test
    void test_upsert() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        repository.upsert(dto);

        verify(valueOperations, times(1)).set(anyString(), any(ThresholdInfoDto.class));
    }

    @Test
    void test_deleteByCultivationId() {
        repository.deleteByCultivationId(1L);

        verify(redisTemplate, times(1)).delete(anyString());
    }

    @Test
    void test_findByCultivationId_success() {
        Object object = new Object();
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(object);
        when(objectMapper.convertValue(object, ThresholdInfoDto.class)).thenReturn(dto);

        Optional<ThresholdInfoDto> result = repository.findByCultivationId(dto.getCultivationId());

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(dto, result.get());
        verify(valueOperations, times(1)).get(anyString());
    }

    @Test
    void test_findByCultivationId_fail() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);

        Optional<ThresholdInfoDto> result = repository.findByCultivationId(dto.getCultivationId());

        Assertions.assertTrue(result.isEmpty());
        verify(objectMapper, never()).convertValue(any(), eq(ThresholdInfoEvent.class));
    }

    @Test
    void test_existsByCultivationId_returnTrue() {
        when(redisTemplate.hasKey(anyString())).thenReturn(true);

        Assertions.assertTrue(repository.existsByCultivationId(1L));
        verify(redisTemplate, times(1)).hasKey(anyString());
    }

    @Test
    void test_existsByCultivationId_returnFalse() {
        when(redisTemplate.hasKey(anyString())).thenReturn(false);

        Assertions.assertFalse(repository.existsByCultivationId(1L));
        verify(redisTemplate, times(1)).hasKey(anyString());
    }

}
