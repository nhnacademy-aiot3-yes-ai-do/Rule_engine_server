package site.yesaido.ruleengine_server.registry.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.yesaido.ruleengine_server.registry.dto.sensor.SensorInfoDeleteEvent;
import site.yesaido.ruleengine_server.global.dto.SensorInfoDto;
import site.yesaido.ruleengine_server.registry.dto.sensor.SensorInfoUpsertEvent;
import site.yesaido.ruleengine_server.registry.repository.ThresholdInfoRepository;
import site.yesaido.ruleengine_server.registry.repository.SensorInfoRepository;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SensorInfoServiceTest {

    @Mock
    private ThresholdInfoRepository thresholdInfoRepository;

    @Mock
    private SensorInfoRepository sensorInfoRepository;

    @InjectMocks
    private SensorInfoService sensorInfoService;

    private SensorInfoUpsertEvent event;

    @BeforeEach
    void setup() {
        event = new SensorInfoUpsertEvent(
                1L,
                "장소", "위치",
                "device_model", "device_name", "device_eui",
                "TEMPERATURE",
                "°C"
        );
    }

    @Test
    void test_upsertSensorInfo() {
        when(thresholdInfoRepository.existsByCultivationId(anyLong())).thenReturn(true);

        sensorInfoService.upsertSensorInfo(event);

        verify(thresholdInfoRepository, times(1)).existsByCultivationId(anyLong());
        verify(sensorInfoRepository, times(1)).upsert(any(SensorInfoDto.class));
    }

    @Test
    @DisplayName("임계값 정보 존재 X, but 센서 정보 upsert 성공 (임계값 정보 도착 지연 고려)")
    void test_upsertSensorInfo_thresholdInfoNotExists_butSuccess() {
        when(thresholdInfoRepository.existsByCultivationId(anyLong())).thenReturn(false);

        sensorInfoService.upsertSensorInfo(event);

        verify(thresholdInfoRepository, times(1)).existsByCultivationId(anyLong());
        verify(sensorInfoRepository, times(1)).upsert(any(SensorInfoDto.class));
    }

    @Test
    void test_findSensorInfo_success() {

        SensorInfoDto dto = SensorInfoDto.from(event);
        when(sensorInfoRepository.findByDeviceEuiAndSensorType(anyString(), anyString(), anyString()))
                .thenReturn(Optional.of(dto));

        Optional<SensorInfoDto> sensorInfoDtoOptional = sensorInfoService.findSensorInfo(event.getDeviceEui(), event.getSensorType(), event.getUnit());

        Assertions.assertTrue(sensorInfoDtoOptional.isPresent());
        Assertions.assertEquals(dto, sensorInfoDtoOptional.get());
        verify(sensorInfoRepository, times(1)).findByDeviceEuiAndSensorType(anyString(), anyString(), anyString());
    }

    @Test
    void test_findSensorInfo_fail() {
        when(sensorInfoRepository.findByDeviceEuiAndSensorType(anyString(), anyString(), anyString()))
                .thenReturn(Optional.empty());

        Optional<SensorInfoDto> sensorInfoDtoOptional = sensorInfoService.findSensorInfo(event.getDeviceEui(), event.getSensorType(), event.getUnit());

        Assertions.assertTrue(sensorInfoDtoOptional.isEmpty());
        verify(sensorInfoRepository, times(1)).findByDeviceEuiAndSensorType(anyString(), anyString(), anyString());
    }

    @Test
    void test_deleteSensorInfo() {
        when(sensorInfoRepository.existsByDeviceEuiAndSensorType(anyString(), anyString(), anyString()))
                .thenReturn(true);

        SensorInfoDeleteEvent deleteDto = new SensorInfoDeleteEvent();
        deleteDto.setCultivationId(1L);
        deleteDto.setDeviceEui("device_eui");
        deleteDto.setSensorType("TEMPERATURE");
        deleteDto.setUnit("°C");
        sensorInfoService.deleteSensorInfo(deleteDto);

        verify(sensorInfoRepository, times(1)).existsByDeviceEuiAndSensorType(anyString(), anyString(), anyString());
        verify(sensorInfoRepository, times(1)).deleteByDeviceEuiAndSensorType(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("센서 정보 존재 X, but 센서 정보 delete 성공 (정보를 전달하는 CultivationService 쪽을 신뢰)")
    void test_deleteSensorInfo_sensorInfoNotExists_butSuccess() {
        when(sensorInfoRepository.existsByDeviceEuiAndSensorType(anyString(), anyString(), anyString()))
                .thenReturn(false);

        SensorInfoDeleteEvent deleteDto = new SensorInfoDeleteEvent();
        deleteDto.setCultivationId(1L);
        deleteDto.setDeviceEui("device_eui");
        deleteDto.setSensorType("TEMPERATURE");
        deleteDto.setUnit("°C");
        sensorInfoService.deleteSensorInfo(deleteDto);

        verify(sensorInfoRepository, times(1)).existsByDeviceEuiAndSensorType(anyString(), anyString(), anyString());
        verify(sensorInfoRepository, times(1)).deleteByDeviceEuiAndSensorType(anyString(), anyString(), anyString());
    }
}
