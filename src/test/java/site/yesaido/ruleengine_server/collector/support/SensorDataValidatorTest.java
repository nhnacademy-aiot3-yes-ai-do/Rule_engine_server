package site.yesaido.ruleengine_server.collector.support;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.yesaido.ruleengine_server.collector.dto.SensorDataDto;
import site.yesaido.ruleengine_server.global.dto.SensorType;
import site.yesaido.ruleengine_server.global.dto.SensorInfoDto;
import site.yesaido.ruleengine_server.registry.service.SensorInfoService;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SensorDataValidatorTest {

    @Mock
    private SensorInfoService sensorInfoService;

    @InjectMocks
    private SensorDataValidator validator;

    private SensorDataDto sensorDataDto;
    private SensorInfoDto sensorInfoDto;

    @BeforeEach
    void setup() {
        sensorDataDto = new SensorDataDto(
                "장소", "위치",
                "device_model", "device_name", "device_eui",
                SensorType.TEMPERATURE,
                20.0, LocalDateTime.now(), ""
        );

        sensorInfoDto = new SensorInfoDto(
                1L,
                "장소", "위치",
                "device_model", "device_name", "device_eui",
                SensorType.TEMPERATURE, ""
        );
    }

    @Test
    void test_isValid_returnTrue() {
        when(sensorInfoService.findSensorInfo(anyString(), any(SensorType.class), ""))
                .thenReturn(Optional.ofNullable(sensorInfoDto));

        boolean result = validator.isValid(sensorDataDto);

        Assertions.assertTrue(result);
        Assertions.assertEquals(sensorDataDto.getCultivationId(), sensorInfoDto.getCultivationId());
        verify(sensorInfoService, times(1)).findSensorInfo(anyString(), any(SensorType.class), "");
    }

    @Test
    void test_isValid_returnFalse_unregisteredSensor() {
        when(sensorInfoService.findSensorInfo(anyString(), any(SensorType.class), "")).thenReturn(Optional.empty());

        boolean result = validator.isValid(sensorDataDto);

        Assertions.assertFalse(result);
        verify(sensorInfoService, times(1)).findSensorInfo(anyString(), any(SensorType.class), "");
    }

    @Test
    void test_isValid_returnFalse_outOfPhysicalRange() {
        when(sensorInfoService.findSensorInfo(anyString(), any(SensorType.class), ""))
                .thenReturn(Optional.ofNullable(sensorInfoDto));

        // 온도 유효 범위: -20.0 ~ 60.0
        sensorDataDto.setValue(100.0);
        boolean temperatureResult = validator.isValid(sensorDataDto);

        // 습도 유효 범위: 0.0 ~ 100.0
        sensorDataDto.setSensorType(SensorType.HUMIDITY);   sensorDataDto.setValue(-10.0);
        boolean humidityResult = validator.isValid(sensorDataDto);

        // co2 유효 범위: 0.0 ~ 10000.0
        sensorDataDto.setSensorType(SensorType.CO2);
        boolean co2Result = validator.isValid(sensorDataDto);

        // 조도 유효 범위: 0.0 ~ 100.0
        sensorDataDto.setSensorType(SensorType.LIGHT);
        boolean lightResult = validator.isValid(sensorDataDto);

        Assertions.assertFalse(temperatureResult);
        Assertions.assertFalse(humidityResult);
        Assertions.assertFalse(co2Result);
        Assertions.assertFalse(lightResult);
        verify(sensorInfoService, times(4)).findSensorInfo(anyString(), any(SensorType.class), "");
    }

    @Test
    void test_isValid_returnFalse_nullValue() {
        when(sensorInfoService.findSensorInfo(anyString(), any(SensorType.class), ""))
                .thenReturn(Optional.ofNullable(sensorInfoDto));

        sensorDataDto.setValue(null);
        boolean result = validator.isValid(sensorDataDto);

        Assertions.assertFalse(result);
        verify(sensorInfoService, times(1)).findSensorInfo(anyString(), any(SensorType.class), "");
    }
}
