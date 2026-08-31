package site.yesaido.ruleengine_server.engine.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import site.yesaido.ruleengine_server.engine.client.DataGeneratorFeignClient;
import site.yesaido.ruleengine_server.engine.dto.actuator.*;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class ActuatorCommandExecutor {

    private static final Duration COMMAND_TTL = Duration.ofSeconds(30);

    private final DataGeneratorFeignClient dataGeneratorFeignClient;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    public ActuatorCommandResult sendCommand(ActuatorControlKey key, ActuatorType actuatorType, ActuatorState desiredState) {

        UUID controlId = UUID.randomUUID();
        Instant requestedAt = Instant.now();
        ActuatorCommandRequest request = new ActuatorCommandRequest(
                controlId,
                UUID.randomUUID(),
                desiredState,
                requestedAt,
                requestedAt.plus(COMMAND_TTL)
        );

        ActuatorCommandResponse response;
        try {
            response = dataGeneratorFeignClient.controlActuator(key.getCultivationId(), actuatorType.name(), request);
        } catch (FeignException e) {
            return handleRejection(key, actuatorType, desiredState, requestedAt, e);
        } catch (Exception e) {
            log.warn("[ActuatorCommandExecutor] Feign 호출 실패: key={}, actuatorType={}, desiredState={}", key, actuatorType, desiredState, e);
            return ActuatorCommandResult.FAILED;
        }

        notificationService.sendActuatorCommandResult(key.getCultivationId(), actuatorType.name(), response, requestedAt);

        return ActuatorCommandResult.APPLIED;
    }

    // ======================================================================

    private ActuatorCommandResult handleRejection(ActuatorControlKey key, ActuatorType actuatorType, ActuatorState desiredState,
                                                   Instant requestedAt, FeignException e) {

        ActuatorCommandResponse rejected;
        try {
            rejected = objectMapper.readValue(e.contentUTF8(), ActuatorCommandResponse.class);
        } catch (Exception parseException) {
            log.warn("[ActuatorCommandExecutor] Feign 호출 실패: key={}, actuatorType={}, desiredState={}", key, actuatorType, desiredState, e);
            return ActuatorCommandResult.FAILED;
        }

        log.warn("[ActuatorCommandExecutor] 명령 거부됨: key={}, actuatorType={}, status={}", key, actuatorType, rejected.status());
        notificationService.sendActuatorCommandResult(key.getCultivationId(), actuatorType.name(), rejected, requestedAt);

        return rejected.status() == ActuatorCommandStatus.REJECTED_CONFLICT
                ? ActuatorCommandResult.REJECTED_CONFLICT
                : ActuatorCommandResult.FAILED;
    }
}
