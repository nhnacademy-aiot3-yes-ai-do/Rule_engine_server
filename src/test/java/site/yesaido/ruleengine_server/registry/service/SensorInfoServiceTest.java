package site.yesaido.ruleengine_server.registry.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.yesaido.ruleengine_server.global.dto.SensorType;
import site.yesaido.ruleengine_server.registry.dto.sensor.SensorInfoDeleteDto;
import site.yesaido.ruleengine_server.registry.dto.sensor.SensorInfoDto;
import site.yesaido.ruleengine_server.registry.repository.CultivationInfoRepository;
import site.yesaido.ruleengine_server.registry.repository.SensorInfoRepository;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SensorInfoServiceTest {

    @Mock
    private CultivationInfoRepository cultivationInfoRepository;

    @Mock
    private SensorInfoRepository sensorInfoRepository;

    @InjectMocks
    private SensorInfoService sensorInfoService;

    @Test
    void test_upsertSensorInfo() {
        when(cultivationInfoRepository.exists(anyLong())).thenReturn(true);

        sensorInfoService.upsertSensorInfo(
                new SensorInfoDto(
                        1L,
                        "장소", "위치",
                        "device_model", "device_eui",
                        SensorType.TEMPERATURE
                )
        );

        verify(cultivationInfoRepository, times(1)).exists(anyLong());
        verify(sensorInfoRepository, times(1)).upsertSensorInfo(any(SensorInfoDto.class));
    }

    @Test
    void test_deleteSensorInfo() {
        when(sensorInfoRepository.exists(anyString(), any(SensorType.class))).thenReturn(true);

        SensorInfoDeleteDto deleteDto = new SensorInfoDeleteDto();
        deleteDto.setCultivationId(1L);
        deleteDto.setDeviceEui("device_eui");
        deleteDto.setSensorType(SensorType.TEMPERATURE);
        sensorInfoService.deleteSensorInfo(deleteDto);

        verify(sensorInfoRepository, times(1)).exists(anyString(), any(SensorType.class));
        verify(sensorInfoRepository, times(1)).deleteSensorInfo(anyString(), any(SensorType.class));
    }
}
