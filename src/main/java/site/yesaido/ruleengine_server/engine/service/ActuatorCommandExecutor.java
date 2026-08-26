package site.yesaido.ruleengine_server.engine.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import site.yesaido.ruleengine_server.engine.client.DataGeneratorFeignClient;
import site.yesaido.ruleengine_server.engine.dto.actuator.*;
import site.yesaido.ruleengine_server.engine.support.ActuatorTypeResolver;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class ActuatorCommandExecutor {

    private static final Duration COMMAND_TTL = Duration.ofSeconds(30);

    private final ActuatorControlStateService actuatorControlStateService;
    private final DataGeneratorFeignClient dataGeneratorFeignClient;
    private final NotificationService notificationService;

    public void requestDirectionChange(ActuatorControlKey key, ActuatorDirection currentDirection, ActuatorDirection targetDirection) {

        actuatorControlStateService.updateDirection(key, ActuatorDirection.PENDING);

        UUID controlId = UUID.randomUUID();

        try {

            if (targetDirection == ActuatorDirection.NONE) {
                boolean offSucceeded = sendCommand(key, controlId, currentDirection, ActuatorState.OFF);
                if (!offSucceeded) {
                    rollback(key, currentDirection);
                    return;
                }

                actuatorControlStateService.updateDirection(key, ActuatorDirection.NONE);
                actuatorControlStateService.clearExceededSince(key);
                return;
            }

            // 반대 방향 전환
            if (currentDirection != ActuatorDirection.NONE && currentDirection != targetDirection) {
                boolean offSucceeded = sendCommand(key, controlId, currentDirection, ActuatorState.OFF);
                if (!offSucceeded) {
                    rollback(key, currentDirection);
                    return;
                }
            }

            boolean onSucceeded = sendCommand(key, controlId, targetDirection, ActuatorState.ON);
            if (!onSucceeded) {
                rollback(key, currentDirection);
                return;
            }

            actuatorControlStateService.updateDirection(key, targetDirection);
            actuatorControlStateService.clearExceededSince(key);
        } catch (Exception e) {
            log.error("[ActuatorCommandExecutor] 액추에이터 제어 중 오류: key={}", key, e);
            rollback(key, currentDirection);
        }
    }

    // ======================================================================

    private boolean sendCommand(ActuatorControlKey key, UUID controlId, ActuatorDirection direction, ActuatorState desiredState) {

        String actuatorType = ActuatorTypeResolver.resolve(key.getSensorType(), direction);

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
            response = dataGeneratorFeignClient.controlActuator(key.getCultivationId(), actuatorType, request);
        } catch (Exception e) {
            log.warn("[ActuatorCommandExecutor] Feign 호출 실패: key={}, actuatorType={}, desiredState={}", key, actuatorType, desiredState, e);
            return false;
        }

        boolean applied = response.status() == ActuatorCommandStatus.APPLIED;
        if(!applied) {
            log.warn("[ActuatorCommandExecutor] 명령 거부됨: key={}, actuatorType={}, status={}", key, actuatorType, response.status());
        }

        notificationService.sendActuatorCommandResult(key.getCultivationId(), actuatorType, response, requestedAt);

        return applied;
    }

    private void rollback(ActuatorControlKey key, ActuatorDirection previousDirection) {

        actuatorControlStateService.updateDirection(key, previousDirection);
    }
}
