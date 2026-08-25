package site.yesaido.ruleengine_server.engine.repository;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import site.yesaido.ruleengine_server.engine.dto.actuator.ActuatorControlKey;
import site.yesaido.ruleengine_server.engine.dto.actuator.ActuatorControlState;
import site.yesaido.ruleengine_server.engine.dto.actuator.ActuatorDirection;
import site.yesaido.ruleengine_server.engine.repository.impl.ActuatorControlStateRedisRepository;
import tools.jackson.databind.ObjectMapper;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActuatorControlStateRepositoryTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ActuatorControlStateRedisRepository repository;

    private ActuatorControlKey key;
    private ActuatorControlState state;

    private static final String KEY = "actuator-control:1:TEMPERATURE";

    @BeforeEach
    void setUp() {
        key = new ActuatorControlKey(1L, "TEMPERATURE");
        state = new ActuatorControlState(ActuatorDirection.INCREASING, OffsetDateTime.now());
    }

    @Test
    void test_find_success() {
        Object rawValue = new Object();
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(KEY)).thenReturn(rawValue);
        when(objectMapper.convertValue(rawValue, ActuatorControlState.class)).thenReturn(state);

        Optional<ActuatorControlState> result = repository.find(key);

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(state, result.get());
        verify(valueOperations, times(1)).get(KEY);
    }

    @Test
    void test_find_whenNotExists_returnsEmpty() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(KEY)).thenReturn(null);

        Optional<ActuatorControlState> result = repository.find(key);

        Assertions.assertTrue(result.isEmpty());
        verify(objectMapper, never()).convertValue(any(), eq(ActuatorControlState.class));
    }

    @Test
    void test_save() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        repository.save(key, state);

        verify(valueOperations, times(1)).set(KEY, state);
    }
}