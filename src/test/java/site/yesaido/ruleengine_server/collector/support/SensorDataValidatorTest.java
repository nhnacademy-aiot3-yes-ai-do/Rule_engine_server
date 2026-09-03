package site.yesaido.ruleengine_server.collector.support;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.yesaido.ruleengine_server.collector.dto.SensorDataDto;
import site.yesaido.ruleengine_server.global.dto.SensorInfoDto;
import site.yesaido.ruleengine_server.registry.service.ManagedSensorTypeService;
import site.yesaido.ruleengine_server.registry.service.SensorInfoService;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SensorDataValidatorTest {

    @Mock
    private SensorInfoService sensorInfoService;

    @Mock
    private ManagedSensorTypeService managedSensorTypeService;

    @InjectMocks
    private SensorDataValidator validator;

    private SensorDataDto sensorDataDto;
    private SensorInfoDto sensorInfoDto;

    @BeforeEach
    void setup() {
        sensorDataDto = new SensorDataDto(
                "장소", "위치",
                "device_model", "device_name", "device_eui",
                "TEMPERATURE",
                BigDecimal.valueOf(20.0), OffsetDateTime.now(ZoneOffset.UTC).withOffsetSameInstant(ZoneOffset.ofHours(9)), "°C"
        );

        sensorInfoDto = new SensorInfoDto(
                1L,
                "장소", "위치",
                "device_model", "device_name", "device_eui",
                "TEMPERATURE", "°C"
        );
    }

    @Test
    @DisplayName("검증 성공")
    void test_isValid_returnTrue() {
        when(managedSensorTypeService.isManaged(anyString())).thenReturn(true);
        when(sensorInfoService.findSensorInfo(anyString(), anyString(), anyString()))
                .thenReturn(Optional.ofNullable(sensorInfoDto));

        Optional<SensorDataDto> result = validator.validate(sensorDataDto);

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(sensorInfoDto.getCultivationId(), result.get().getCultivationId());
        verify(managedSensorTypeService, times(1)).isManaged(anyString());
        verify(sensorInfoService, times(1)).findSensorInfo(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("검증 실패: 관리되고 있지 않은 센서 타입")
    void test_isValid_returnFalse_sensorTypeNotManaged() {
        when(managedSensorTypeService.isManaged(anyString())).thenReturn(false);

        Optional<SensorDataDto> result = validator.validate(sensorDataDto);

        Assertions.assertTrue(result.isEmpty());
        verify(managedSensorTypeService, times(1)).isManaged(anyString());
        verify(sensorInfoService, never()).findSensorInfo(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("검증 실패: 센서 값이 null")
    void test_isValid_returnFalse_nullValue() {
        when(managedSensorTypeService.isManaged(anyString())).thenReturn(true);

        SensorDataDto nullValueDto = new SensorDataDto(
                "장소", "위치",
                "device_model", "device_name", "device_eui",
                "TEMPERATURE",
                null, OffsetDateTime.now(ZoneOffset.UTC).withOffsetSameInstant(ZoneOffset.ofHours(9)), "°C"
        );
        Optional<SensorDataDto> result = validator.validate(nullValueDto);

        Assertions.assertTrue(result.isEmpty());
        verify(managedSensorTypeService, times(1)).isManaged(anyString());
        verify(sensorInfoService, never()).findSensorInfo(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("검증 실패: 등록되지 않은 센서")
    void test_isValid_returnFalse_unregisteredSensor() {
        when(managedSensorTypeService.isManaged(anyString())).thenReturn(true);
        when(sensorInfoService.findSensorInfo(anyString(), anyString(), anyString())).thenReturn(Optional.empty());

        Optional<SensorDataDto> result = validator.validate(sensorDataDto);

        Assertions.assertTrue(result.isEmpty());
        verify(managedSensorTypeService, times(1)).isManaged(anyString());
        verify(sensorInfoService, times(1)).findSensorInfo(anyString(), anyString(), anyString());
    }
}
