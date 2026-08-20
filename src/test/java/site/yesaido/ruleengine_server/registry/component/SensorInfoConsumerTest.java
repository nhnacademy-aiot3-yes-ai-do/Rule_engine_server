package site.yesaido.ruleengine_server.registry.component;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.yesaido.ruleengine_server.registry.dto.sensor.SensorInfoDeleteEvent;
import site.yesaido.ruleengine_server.registry.dto.sensor.SensorInfoUpsertEvent;
import site.yesaido.ruleengine_server.registry.service.SensorInfoService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SensorInfoConsumerTest {

    @Mock
    private SensorInfoService sensorInfoService;

    @InjectMocks
    private SensorInfoConsumer sensorInfoConsumer;

    @Test
    void test_consumeSensorInfoUpsertEvent() {

        SensorInfoUpsertEvent event = mock(SensorInfoUpsertEvent.class);

        sensorInfoConsumer.consumeSensorInfoUpsertEvent(event);

        verify(sensorInfoService, times(1))
                .upsertSensorInfo(any(SensorInfoUpsertEvent.class));
    }

    @Test
    void test_consumeSensorInfoDeleteEvent() {

        SensorInfoDeleteEvent deleteEvent = mock(SensorInfoDeleteEvent.class);

        sensorInfoConsumer.consumeSensorInfoDeleteEvent(deleteEvent);

        verify(sensorInfoService, times(1))
                .deleteSensorInfo(any(SensorInfoDeleteEvent.class));
    }
}
