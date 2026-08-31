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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActuatorCommandExecutorTest {

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
    void test_sendCommand_whenApplied_returnsTrue() {
        when(dataGeneratorFeignClient.controlActuator(any(), any(), any()))
                .thenReturn(new ActuatorCommandResponse(
                        UUID.randomUUID(), UUID.randomUUID(),
                        ActuatorCommandStatus.APPLIED, ActuatorState.ON, Instant.now()));

        boolean result = actuatorCommandExecutor.sendCommand(key, ActuatorType.HEATER, ActuatorState.ON);

        assertTrue(result);
        verify(dataGeneratorFeignClient, times(1)).controlActuator(eq(1L), eq("HEATER"), any());
        verify(notificationService, times(1)).sendActuatorCommandResult(eq(1L), eq("HEATER"), any(), any());
    }

    @Test
    void test_sendCommand_whenRejected_returnsFalseButStillNotifies() {
        when(dataGeneratorFeignClient.controlActuator(any(), any(), any()))
                .thenReturn(new ActuatorCommandResponse(
                        UUID.randomUUID(), UUID.randomUUID(),
                        ActuatorCommandStatus.REJECTED_CONFLICT, ActuatorState.OFF, null));

        boolean result = actuatorCommandExecutor.sendCommand(key, ActuatorType.COOLER, ActuatorState.ON);

        assertFalse(result);
        verify(notificationService, times(1)).sendActuatorCommandResult(eq(1L), eq("COOLER"), any(), any());
    }

    @Test
    void test_sendCommand_whenFeignThrows_returnsFalseWithoutNotifying() {
        when(dataGeneratorFeignClient.controlActuator(any(), any(), any()))
                .thenThrow(new RuntimeException("connection refused"));

        boolean result = actuatorCommandExecutor.sendCommand(key, ActuatorType.HEATER, ActuatorState.OFF);

        assertFalse(result);
        verifyNoInteractions(notificationService);
    }

    @Test
    void test_sendCommand_usesActuatorTypeNameAsPathVariable() {
        when(dataGeneratorFeignClient.controlActuator(any(), any(), any()))
                .thenReturn(new ActuatorCommandResponse(
                        UUID.randomUUID(), UUID.randomUUID(),
                        ActuatorCommandStatus.APPLIED, ActuatorState.ON, Instant.now()));

        actuatorCommandExecutor.sendCommand(key, ActuatorType.DEHUMIDIFIER, ActuatorState.ON);

        verify(dataGeneratorFeignClient).controlActuator(eq(1L), eq("DEHUMIDIFIER"), any());
    }
}
