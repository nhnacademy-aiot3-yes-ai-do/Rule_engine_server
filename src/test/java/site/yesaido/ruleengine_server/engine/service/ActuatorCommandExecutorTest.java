package site.yesaido.ruleengine_server.engine.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import feign.FeignException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import site.yesaido.ruleengine_server.engine.client.DataGeneratorFeignClient;
import site.yesaido.ruleengine_server.engine.dto.actuator.*;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActuatorCommandExecutorTest {

    @Mock
    private DataGeneratorFeignClient dataGeneratorFeignClient;

    @Mock
    private NotificationService notificationService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @InjectMocks
    private ActuatorCommandExecutor actuatorCommandExecutor;

    private ActuatorControlKey key;

    @BeforeEach
    void setUp() {
        key = new ActuatorControlKey(1L, "TEMPERATURE");
    }

    @Test
    void test_sendCommand_whenApplied_returnsApplied() {
        when(dataGeneratorFeignClient.controlActuator(any(), any(), any()))
                .thenReturn(new ActuatorCommandResponse(
                        UUID.randomUUID(), UUID.randomUUID(),
                        ActuatorCommandStatus.APPLIED, ActuatorState.ON, Instant.now()));

        ActuatorCommandResult result = actuatorCommandExecutor.sendCommand(key, ActuatorType.HEATER, ActuatorState.ON);

        assertEquals(ActuatorCommandResult.APPLIED, result);
        verify(notificationService, times(1)).sendActuatorCommandResult(eq(1L), eq("HEATER"), any(), any());
    }

    @Test
    void test_sendCommand_whenRejectedConflict_returnsRejectedConflict() {
        when(dataGeneratorFeignClient.controlActuator(any(), any(), any()))
                .thenThrow(conflictException("REJECTED_CONFLICT"));

        ActuatorCommandResult result = actuatorCommandExecutor.sendCommand(key, ActuatorType.COOLER, ActuatorState.ON);

        assertEquals(ActuatorCommandResult.REJECTED_CONFLICT, result);
        verify(notificationService, times(1)).sendActuatorCommandResult(eq(1L), eq("COOLER"), any(), any());
    }

    @Test
    void test_sendCommand_whenRejectedStale_returnsFailedNotConflict() {
        when(dataGeneratorFeignClient.controlActuator(any(), any(), any()))
                .thenThrow(conflictException("REJECTED_STALE"));

        ActuatorCommandResult result = actuatorCommandExecutor.sendCommand(key, ActuatorType.COOLER, ActuatorState.ON);

        assertEquals(ActuatorCommandResult.FAILED, result);
        verify(notificationService, times(1)).sendActuatorCommandResult(eq(1L), eq("COOLER"), any(), any());
    }

    @Test
    void test_sendCommand_whenFeignExceptionBodyUnparseable_returnsFailedWithoutNotifying() {
        when(dataGeneratorFeignClient.controlActuator(any(), any(), any()))
                .thenThrow(rawFeignException("not a json body"));

        ActuatorCommandResult result = actuatorCommandExecutor.sendCommand(key, ActuatorType.HEATER, ActuatorState.ON);

        assertEquals(ActuatorCommandResult.FAILED, result);
        verifyNoInteractions(notificationService);
    }

    @Test
    void test_sendCommand_whenConnectionFails_returnsFailedWithoutNotifying() {
        when(dataGeneratorFeignClient.controlActuator(any(), any(), any()))
                .thenThrow(new RuntimeException("connection refused"));

        ActuatorCommandResult result = actuatorCommandExecutor.sendCommand(key, ActuatorType.HEATER, ActuatorState.OFF);

        assertEquals(ActuatorCommandResult.FAILED, result);
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

    // ======================================================================

    private FeignException conflictException(String status) {
        String body = """
                {"controlId":"%s","commandId":"%s","status":"%s","actualState":"OFF","appliedAt":null}
                """.formatted(UUID.randomUUID(), UUID.randomUUID(), status);
        return rawFeignException(body);
    }

    private FeignException rawFeignException(String body) {
        Request request = Request.create(
                Request.HttpMethod.PUT,
                "/api/v1/internal/cultivations/1/actuators/HEATER/state",
                Map.of(), null, StandardCharsets.UTF_8, null);

        Response response = Response.builder()
                .status(409)
                .reason("Conflict")
                .request(request)
                .headers(Map.of())
                .body(body, StandardCharsets.UTF_8)
                .build();

        return FeignException.errorStatus("DataGeneratorFeignClient#controlActuator", response);
    }
}
