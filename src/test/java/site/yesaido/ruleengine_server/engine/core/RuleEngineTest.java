//package site.yesaido.ruleengine_server.engine.core;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import site.yesaido.ruleengine_server.collector.dto.SensorDataDto;
//import site.yesaido.ruleengine_server.engine.service.InfluxService;
//import site.yesaido.ruleengine_server.global.dto.SensorValueEvent;
//import site.yesaido.ruleengine_server.global.dto.ThresholdInfoDto;
//import site.yesaido.ruleengine_server.registry.service.ThresholdInfoService;
//
//import java.math.BigDecimal;
//import java.time.OffsetDateTime;
//import java.time.ZoneOffset;
//import java.util.Optional;
//
//import static org.mockito.ArgumentMatchers.anyLong;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class RuleEngineTest {
//
//    @Mock
//    private ThresholdInfoService thresholdInfoService;
//
//    @Mock
//    private InfluxService influxService;
//
//    @InjectMocks
//    private RuleEngine ruleEngine;
//
//    private SensorDataDto dto;
//
//    @BeforeEach
//    void setup() {
//        dto = new SensorDataDto(
//                "장소", "위치",
//                "device_model", "device_name", "device_eui",
//                "TEMPERATURE",
//                BigDecimal.valueOf(22.2), OffsetDateTime.now(ZoneOffset.UTC).withOffsetSameInstant(ZoneOffset.ofHours(9)), "°C",
//                1L
//        );
//    }
//
//    @Test
//    @DisplayName("실시간 데이터 발행 성공")
//    void test_publishRealTimeData_success() {
//        ThresholdInfoDto thresholdInfoDto = mock(ThresholdInfoDto.class);
//        when(thresholdInfoService.findCultivationInfo(anyLong())).thenReturn(Optional.of(thresholdInfoDto));
//
//        ruleEngine.start(dto);
//
//        verify(thresholdInfoService, times(1)).findCultivationInfo(anyLong());
//        verify(influxService, times(1)).save(any(SensorValueEvent.class));
//
//    }
//
//    @Test
//    @DisplayName("실시간 데이터 발행 실패 : 임계값 정보를 찾을 수 없는 경우")
//    void test_publishRealTimeData_fail_whenThresholdInfoNotFound() {
//        when(thresholdInfoService.findCultivationInfo(anyLong())).thenReturn(Optional.empty());
//
//        ruleEngine.start(dto);
//
//        verify(thresholdInfoService, times(1)).findCultivationInfo(anyLong());
//        verify(influxService, never()).save(any(SensorValueEvent.class));
//    }
//}
