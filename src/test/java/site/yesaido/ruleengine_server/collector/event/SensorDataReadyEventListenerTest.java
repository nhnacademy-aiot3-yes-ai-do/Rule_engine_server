package site.yesaido.ruleengine_server.collector.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.yesaido.ruleengine_server.collector.dto.SensorDataDto;
import site.yesaido.ruleengine_server.engine.core.RuleEngine;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SensorDataReadyEventListenerTest {

    @Mock
    private RuleEngine ruleEngine;

    @InjectMocks
    private SensorDataReadyEventListener eventListener;

    @Test
    void test_handleSensorDataReady() {
        SensorDataDto dto = mock(SensorDataDto.class);
        SensorDataReadyEvent event = new SensorDataReadyEvent(dto);

        eventListener.handleSensorDataReady(event);

        verify(ruleEngine, times(1)).start(dto);
    }

    @Test
    void test_handleSensorDataReady_fail() {
        SensorDataDto dto = mock(SensorDataDto.class);
        SensorDataReadyEvent event = new SensorDataReadyEvent(dto);

        doThrow(new RuntimeException("RuleEngine Exception!")).when(ruleEngine).start(dto);

        eventListener.handleSensorDataReady(event);

        verify(ruleEngine, times(1)).start(dto);
    }
}
