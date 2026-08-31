package site.yesaido.ruleengine_server.engine.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;
import site.yesaido.ruleengine_server.engine.dto.actuator.ActuatorControlKey;
import site.yesaido.ruleengine_server.engine.dto.actuator.ActuatorControlState;
import site.yesaido.ruleengine_server.engine.service.ActuatorControlStateService;

@Service
public class CaffeineActuatorControlStateService implements ActuatorControlStateService {

    private final Cache<ActuatorControlKey, ActuatorControlState> stateCache = Caffeine.newBuilder().build();

    @Override
    public ActuatorControlState getState(ActuatorControlKey key) {

        ActuatorControlState state = stateCache.getIfPresent(key);
        return state != null ? state : new ActuatorControlState.None();
    }

    @Override
    public void setState(ActuatorControlKey key, ActuatorControlState state) {
        stateCache.put(key, state);
    }
}
