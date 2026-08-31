package site.yesaido.ruleengine_server.engine.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import site.yesaido.ruleengine_server.engine.dto.actuator.ActuatorControlKey;
import site.yesaido.ruleengine_server.engine.dto.actuator.ActuatorControlState;
import site.yesaido.ruleengine_server.engine.dto.actuator.ActuatorType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class CaffeineActuatorControlStateServiceTest {

    private CaffeineActuatorControlStateService service;
    private ActuatorControlKey key;

    @BeforeEach
    void setUp() {
        service = new CaffeineActuatorControlStateService();
        key = new ActuatorControlKey(1L, "TEMPERATURE");
    }

    @Test
    void test_getState_whenNeverSet_returnsNone() {
        ActuatorControlState state = service.getState(key);

        assertInstanceOf(ActuatorControlState.None.class, state);
    }

    @Test
    void test_getState_afterSetState_returnsSameState() {
        service.setState(key, new ActuatorControlState.Active(ActuatorType.HEATER));

        ActuatorControlState state = service.getState(key);

        assertEquals(new ActuatorControlState.Active(ActuatorType.HEATER), state);
    }

    @Test
    void test_setState_overwritesExistingState() {
        service.setState(key, new ActuatorControlState.Active(ActuatorType.HEATER));
        service.setState(key, new ActuatorControlState.Pending());

        ActuatorControlState state = service.getState(key);

        assertInstanceOf(ActuatorControlState.Pending.class, state);
    }

    @Test
    void test_getState_isIsolatedPerKey() {
        ActuatorControlKey otherKey = new ActuatorControlKey(1L, "HUMIDITY");
        service.setState(key, new ActuatorControlState.Active(ActuatorType.HEATER));

        ActuatorControlState otherState = service.getState(otherKey);

        assertInstanceOf(ActuatorControlState.None.class, otherState);
    }
}
