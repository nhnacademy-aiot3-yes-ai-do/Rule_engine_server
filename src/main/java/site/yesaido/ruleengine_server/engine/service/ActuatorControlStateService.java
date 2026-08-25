package site.yesaido.ruleengine_server.engine.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import site.yesaido.ruleengine_server.engine.dto.actuator.ActuatorControlKey;
import site.yesaido.ruleengine_server.engine.dto.actuator.ActuatorControlState;
import site.yesaido.ruleengine_server.engine.dto.actuator.ActuatorDirection;
import site.yesaido.ruleengine_server.engine.repository.ActuatorControlStateRepository;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@RequiredArgsConstructor
@Service
public class ActuatorControlStateService {

    private final ActuatorControlStateRepository actuatorControlStateRepository;

    public ActuatorControlState getState(ActuatorControlKey key) {

        return actuatorControlStateRepository.find(key).
                orElse(new ActuatorControlState(ActuatorDirection.NONE, null));
    }

    public void resetExceededSince(ActuatorControlKey key) {

        ActuatorControlState state = getState(key);
        state.setExceededSince(now());
        actuatorControlStateRepository.save(key, state);
    }

    public void updateDirection(ActuatorControlKey key, ActuatorDirection direction) {

        ActuatorControlState state = getState(key);
        state.setActuatorDirection(direction);
        actuatorControlStateRepository.save(key, state);
    }

    public void clearExceededSince(ActuatorControlKey key) {

        ActuatorControlState state = getState(key);
        state.setExceededSince(null);
        actuatorControlStateRepository.save(key, state);
    }

    // ======================================================================

    private OffsetDateTime now() {

        return OffsetDateTime.now(ZoneOffset.UTC).withOffsetSameInstant(ZoneOffset.ofHours(9));
    }
}
