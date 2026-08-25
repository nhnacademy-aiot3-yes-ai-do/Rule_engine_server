package site.yesaido.ruleengine_server.engine.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.yesaido.ruleengine_server.engine.client.DataGeneratorFeignClient;
import site.yesaido.ruleengine_server.engine.dto.actuator.*;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActuatorCommandExecutorTest {

    @Mock
    private ActuatorControlStateService actuatorControlStateService;
    @Mock
    private DataGeneratorFeignClient dataGeneratorFeignClient;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ActuatorCommandExecutor actuatorCommandExecutor;

    private ActuatorControlKey key;

    @BeforeEach
    void setUp() {
        key = new ActuatorControlKey(1L, "TEMPERATURE");
    }

    @Test
    void test_requestDirectionChange_fromNone_toIncreasing_success() {
        when(dataGeneratorFeignClient.controlActuator(any(), any(), any()))
                .thenReturn(new ActuatorCommandResponse(
                        UUID.randomUUID(), UUID.randomUUID(),
                        ActuatorCommandStatus.APPLIED, ActuatorState.ON, Instant.now()
                ));

        actuatorCommandExecutor.requestDirectionChange(key, ActuatorDirection.NONE, ActuatorDirection.INCREASING);

        // PENDING -> INCREASING 순으로 갱신됐는지
        verify(actuatorControlStateService).updateDirection(key, ActuatorDirection.PENDING);
        verify(actuatorControlStateService).updateDirection(key, ActuatorDirection.INCREASING);
        // Feign은 한 번만 호출(반대 방향 전환 없으므로 OFF 요청 없음)
        verify(dataGeneratorFeignClient, times(1)).controlActuator(any(), any(), any());
    }

    @Test
    void test_requestDirectionChange_oppositeDirection_sendsOffThenOn() {
        when(dataGeneratorFeignClient.controlActuator(any(), any(), any()))
                .thenReturn(new ActuatorCommandResponse(
                        UUID.randomUUID(), UUID.randomUUID(),
                        ActuatorCommandStatus.APPLIED, ActuatorState.ON, Instant.now()
                ));

        actuatorCommandExecutor.requestDirectionChange(key, ActuatorDirection.DECREASING, ActuatorDirection.INCREASING);

        // OFF(COOLER) + ON(HEATER) 두 번 호출됐는지
        verify(dataGeneratorFeignClient, times(2)).controlActuator(any(), any(), any());
    }

    @Test
    void test_requestDirectionChange_whenOffFails_rollsBackWithoutCallingOn() {
        when(dataGeneratorFeignClient.controlActuator(any(), any(), any()))
                .thenReturn(new ActuatorCommandResponse(
                        UUID.randomUUID(), UUID.randomUUID(),
                        ActuatorCommandStatus.REJECTED_CONFLICT, ActuatorState.ON, null
                ));

        actuatorCommandExecutor.requestDirectionChange(key, ActuatorDirection.DECREASING, ActuatorDirection.INCREASING);

        // OFF 실패 시 ON을 시도하면 안 됨 -> 딱 1번만 호출
        verify(dataGeneratorFeignClient, times(1)).controlActuator(any(), any(), any());
        verify(actuatorControlStateService).updateDirection(key, ActuatorDirection.DECREASING); // 롤백
    }

    @Test
    void test_requestDirectionChange_toNone_sendsOffOnly() {
        when(dataGeneratorFeignClient.controlActuator(any(), any(), any()))
                .thenReturn(new ActuatorCommandResponse(
                        UUID.randomUUID(), UUID.randomUUID(),
                        ActuatorCommandStatus.APPLIED, ActuatorState.OFF, Instant.now()
                ));

        actuatorCommandExecutor.requestDirectionChange(key, ActuatorDirection.INCREASING, ActuatorDirection.NONE);

        verify(dataGeneratorFeignClient, times(1)).controlActuator(any(), any(), any());
        verify(actuatorControlStateService).updateDirection(key, ActuatorDirection.NONE);
        verify(actuatorControlStateService).clearExceededSince(key);
    }
}