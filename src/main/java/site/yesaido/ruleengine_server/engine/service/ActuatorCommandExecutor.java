package site.yesaido.ruleengine_server.engine.service;

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

    public boolean sendCommand(ActuatorControlKey key, ActuatorType actuatorType, ActuatorState desiredState) {

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
        } catch (Exception e) {
            log.warn("[ActuatorCommandExecutor] Feign 호출 실패: key={}, actuatorType={}, desiredState={}", key, actuatorType, desiredState, e);
            return false;
        }

        boolean applied = response.status() == ActuatorCommandStatus.APPLIED;
        if (!applied) {
            log.warn("[ActuatorCommandExecutor] 명령 거부됨: key={}, actuatorType={}, status={}", key, actuatorType, response.status());
        }

        notificationService.sendActuatorCommandResult(key.getCultivationId(), actuatorType.name(), response, requestedAt);

        return applied;
    }

}
