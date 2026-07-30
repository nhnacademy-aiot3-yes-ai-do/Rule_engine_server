package site.yesaido.ruleengine_server.registry.component;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.yesaido.ruleengine_server.global.dto.SensorType;
import site.yesaido.ruleengine_server.registry.dto.sensor.SensorInfoDeleteDto;
import site.yesaido.ruleengine_server.registry.dto.sensor.SensorInfoDto;
import site.yesaido.ruleengine_server.registry.service.SensorInfoService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SensorInfoConsumerTest {

    @Mock
    private SensorInfoService sensorInfoService;

    @InjectMocks
    private SensorInfoConsumer sensorInfoConsumer;

    @Test
    void test_consumeSensorInfoUpsert() {
        SensorInfoDto dto = new SensorInfoDto(
                1L,
                "장소", "위치",
                "device_model", "device_eui",
                SensorType.TEMPERATURE
        );

        sensorInfoConsumer.consumeSensorInfoUpsert(dto);

        verify(sensorInfoService, times(1))
                .upsertSensorInfo(any(SensorInfoDto.class));
    }

    @Test
    void test_consumeSensorInfoDelete() {
        SensorInfoDeleteDto deleteDto = new SensorInfoDeleteDto();
        deleteDto.setCultivationId(1L);
        deleteDto.setSensorType(SensorType.TEMPERATURE);

        sensorInfoConsumer.consumeSensorInfoDelete(deleteDto);

        verify(sensorInfoService, times(1))
                .deleteSensorInfo(deleteDto);
    }
}
