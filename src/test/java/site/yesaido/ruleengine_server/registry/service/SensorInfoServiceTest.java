package site.yesaido.ruleengine_server.registry.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.yesaido.ruleengine_server.global.dto.SensorType;
import site.yesaido.ruleengine_server.registry.dto.sensor.SensorInfoDeleteEvent;
import site.yesaido.ruleengine_server.global.dto.SensorInfoDto;
import site.yesaido.ruleengine_server.registry.repository.CultivationInfoRepository;
import site.yesaido.ruleengine_server.registry.repository.SensorInfoRepository;

import java.util.Optional;

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

    private SensorInfoDto dto;

    @BeforeEach
    void setup() {
        dto = new SensorInfoDto(
                1L,
                "장소", "위치",
                "device_model", "device_name", "device_eui",
                SensorType.TEMPERATURE
        );
    }

    @Test
    void test_upsertSensorInfo() {
        when(cultivationInfoRepository.exists(anyLong())).thenReturn(true);

        sensorInfoService.upsertSensorInfo(dto);

        verify(cultivationInfoRepository, times(1)).exists(anyLong());
        verify(sensorInfoRepository, times(1)).upsertSensorInfo(any(SensorInfoDto.class));
    }

    @Test
    void test_findSensorInfo_success() {
        when(sensorInfoRepository.findSensorInfo(anyString(), any(SensorType.class)))
                .thenReturn(Optional.ofNullable(dto));

        Optional<SensorInfoDto> sensorInfoDtoOptional = sensorInfoService.findSensorInfo(dto.getDeviceEui(), dto.getSensorType());

        Assertions.assertTrue(sensorInfoDtoOptional.isPresent());
        verify(sensorInfoRepository, times(1)).findSensorInfo(anyString(), any(SensorType.class));
    }

    @Test
    void test_findSensorInfo_fail() {
        when(sensorInfoRepository.findSensorInfo(anyString(), any(SensorType.class))).thenReturn(Optional.empty());

        Optional<SensorInfoDto> sensorInfoDtoOptional = sensorInfoService.findSensorInfo(dto.getDeviceEui(), dto.getSensorType());

        Assertions.assertTrue(sensorInfoDtoOptional.isEmpty());
        verify(sensorInfoRepository, times(1)).findSensorInfo(anyString(), any(SensorType.class));
    }

    @Test
    void test_deleteSensorInfo() {
        when(sensorInfoRepository.exists(anyString(), any(SensorType.class))).thenReturn(true);

        SensorInfoDeleteEvent deleteDto = new SensorInfoDeleteEvent();
        deleteDto.setCultivationId(1L);
        deleteDto.setDeviceEui("device_eui");
        deleteDto.setSensorType(SensorType.TEMPERATURE);
        sensorInfoService.deleteSensorInfo(deleteDto);

        verify(sensorInfoRepository, times(1)).exists(anyString(), any(SensorType.class));
        verify(sensorInfoRepository, times(1)).deleteSensorInfo(anyString(), any(SensorType.class));
    }
}
