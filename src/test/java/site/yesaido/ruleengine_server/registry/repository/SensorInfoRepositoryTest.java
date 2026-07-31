package site.yesaido.ruleengine_server.registry.repository;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import site.yesaido.ruleengine_server.global.dto.SensorType;
import site.yesaido.ruleengine_server.registry.dto.sensor.SensorInfoDto;
import site.yesaido.ruleengine_server.registry.repository.impl.SensorInfoRedisRepository;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SensorInfoRepositoryTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private SensorInfoRedisRepository repository;

    private SensorInfoDto dto;

    @BeforeEach
    void setUp() {
        dto = new SensorInfoDto(
                1L,
                "장소", "위치",
                "device_model", "device_name", "device_eui",
                SensorType.TEMPERATURE
        );
    }

    @Test
    void test_upsertSensorInfo() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        repository.upsertSensorInfo(dto);

        verify(valueOperations, times(1)).set(anyString(), any(SensorInfoDto.class));
    }

    @Test
    void test_deleteSensorInfo() {
        repository.deleteSensorInfo(dto.getDeviceEui(), dto.getSensorType());

        verify(redisTemplate, times(1)).delete(anyString());
    }

    @Test
    void test_exists_returnTrue() {
        when(redisTemplate.hasKey(anyString())).thenReturn(true);

        Assertions.assertTrue(repository.exists(dto.getDeviceEui(), dto.getSensorType()));
        verify(redisTemplate, times(1)).hasKey(anyString());
    }

    @Test
    void test_exists_returnFalse() {
        when(redisTemplate.hasKey(anyString())).thenReturn(false);

        Assertions.assertFalse(repository.exists(dto.getDeviceEui(), dto.getSensorType()));
        verify(redisTemplate, times(1)).hasKey(anyString());
    }
}
