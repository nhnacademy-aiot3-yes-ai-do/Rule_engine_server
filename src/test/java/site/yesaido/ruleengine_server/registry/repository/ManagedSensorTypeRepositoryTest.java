package site.yesaido.ruleengine_server.registry.repository;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import site.yesaido.ruleengine_server.registry.repository.impl.ManagedSensorTypeRedisRepository;

import java.util.Set;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ManagedSensorTypeRepositoryTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private SetOperations<String, Object> setOperations;

    @InjectMocks
    private ManagedSensorTypeRedisRepository repository;

    private static final String KEY = "managed-sensor-types";

    @BeforeEach
    void setup() {
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
    }

    @Test
    void test_register() {

        repository.register("A");

        verify(setOperations, times(1)).add(KEY, "A");
    }

    @Test
    void test_isManaged_returnTrue() {

        when(setOperations.isMember(KEY, "A")).thenReturn(true);

        Assertions.assertTrue(repository.isManaged("A"));
        verify(setOperations, times(1)).isMember(KEY, "A");
    }

    @Test
    void test_isManaged_returnFalse() {
        when(setOperations.isMember(KEY, "A")).thenReturn(false);

        Assertions.assertFalse(repository.isManaged("A"));
    }

    @Test
    void test_isManaged_whenNullReturned_returnFalse() {
        when(setOperations.isMember(KEY, "A")).thenReturn(null);

        Assertions.assertFalse(repository.isManaged("A"));
    }

    @Test
    void test_findAll_success() {
        when(setOperations.members(KEY)).thenReturn(Set.of("A", "B"));

        Set<String> result = repository.findAll();

        Assertions.assertEquals(2, result.size());
        Assertions.assertTrue(result.contains("A"));
        Assertions.assertTrue(result.contains("B"));
    }

    @Test
    void test_findAll_whenNull_returnsEmptySet() {
        when(setOperations.members(KEY)).thenReturn(null);

        Set<String> result = repository.findAll();

        Assertions.assertTrue(result.isEmpty());
    }
}