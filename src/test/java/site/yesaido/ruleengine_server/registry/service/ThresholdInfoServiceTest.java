package site.yesaido.ruleengine_server.registry.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.yesaido.ruleengine_server.global.dto.SensorType;
import site.yesaido.ruleengine_server.global.dto.ThresholdInfoDto;
import site.yesaido.ruleengine_server.global.exception.ThresholdInfoNotFoundException;
import site.yesaido.ruleengine_server.registry.dto.threshold.SensorRange;
import site.yesaido.ruleengine_server.registry.dto.threshold.ThresholdInfoEvent;
import site.yesaido.ruleengine_server.registry.repository.ThresholdInfoRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ThresholdInfoServiceTest {

    @Mock
    private ThresholdInfoRepository thresholdInfoRepository;

    @InjectMocks
    private ThresholdInfoService thresholdInfoService;

    private ThresholdInfoEvent event;
    private List<SensorRange> sensorRangeList;
    private ThresholdInfoDto dto;

    @BeforeEach
    void setUp() {
        sensorRangeList = new ArrayList<>();
        sensorRangeList.add(new SensorRange(SensorType.TEMPERATURE, "°C", BigDecimal.valueOf(20.0), BigDecimal.valueOf(30.0)));
        sensorRangeList.add(new SensorRange(SensorType.HUMIDITY, "%", BigDecimal.valueOf(60.0), BigDecimal.valueOf(80.0)));
        sensorRangeList.add(new SensorRange(SensorType.CO2, "ppm", BigDecimal.valueOf(600.0), BigDecimal.valueOf(800.0)));
        sensorRangeList.add(new SensorRange(SensorType.LIGHT, "lx", BigDecimal.valueOf(0.0), BigDecimal.valueOf(500.0)));
        event = new ThresholdInfoEvent(
                1L,
                sensorRangeList
        );

        dto = ThresholdInfoDto.from(event.getCultivationId(), event.getSensorRangeList());
    }

    @Test
    void test_processThresholdInfoEvent_whenSensorRangeListSizeIsFour() {

        thresholdInfoService.processThresholdInfoEvent(event);

        verify(thresholdInfoRepository, times(1)).upsert(any(ThresholdInfoDto.class));

        verify(thresholdInfoRepository, never()).findByCultivationId(anyLong());
        verify(thresholdInfoRepository, never()).deleteByCultivationId(anyLong());
    }

    @Test
    void test_processThresholdInfoEvent_whenSensorRangeListSizeIsOne() {

        SensorRange rangeToUpdate = new SensorRange(SensorType.TEMPERATURE, "°C", BigDecimal.valueOf(20.0), BigDecimal.valueOf(30.0));
        ThresholdInfoEvent updateEvent = new ThresholdInfoEvent(1L, List.of(rangeToUpdate));

        ThresholdInfoDto existingDto = mock(ThresholdInfoDto.class);
        when(thresholdInfoRepository.findByCultivationId(anyLong())).thenReturn(Optional.ofNullable(existingDto));

        thresholdInfoService.processThresholdInfoEvent(updateEvent);

        verify(thresholdInfoRepository, times(1)).findByCultivationId(anyLong());
        verify(existingDto, times(1)).applyRange(rangeToUpdate);
        verify(thresholdInfoRepository, times(1)).upsert(any(ThresholdInfoDto.class));

        verify(thresholdInfoRepository, never()).deleteByCultivationId(anyLong());
    }

    @Test
    void test_processThresholdInfoEvent_whenSensorRangeListSizeIsOne_fail() {

        SensorRange rangeToUpdate = new SensorRange(SensorType.TEMPERATURE, "°C", BigDecimal.valueOf(20.0), BigDecimal.valueOf(30.0));
        ThresholdInfoEvent updateEvent = new ThresholdInfoEvent(1L, List.of(rangeToUpdate));

        when(thresholdInfoRepository.findByCultivationId(anyLong())).thenReturn(Optional.empty());

        Assertions.assertThrows(ThresholdInfoNotFoundException.class, () -> thresholdInfoService.processThresholdInfoEvent(updateEvent));

        verify(thresholdInfoRepository, times(1)).findByCultivationId(anyLong());
        verify(thresholdInfoRepository, never()).upsert(any(ThresholdInfoDto.class));
        verify(thresholdInfoRepository, never()).deleteByCultivationId(anyLong());
    }

    @Test
    void test_processThresholdInfoEvent_whenSensorRangeListSizeIsZero() {

        ThresholdInfoEvent deleteEvent = new ThresholdInfoEvent(1L, Collections.emptyList());

        thresholdInfoService.processThresholdInfoEvent(deleteEvent);

        verify(thresholdInfoRepository, times(1)).deleteByCultivationId(anyLong());
        verify(thresholdInfoRepository, never()).upsert(any(ThresholdInfoDto.class));
        verify(thresholdInfoRepository, never()).findByCultivationId(anyLong());
    }

    @Test
    void test_findCultivationInfo_success() {
        when(thresholdInfoRepository.findByCultivationId(anyLong())).thenReturn(Optional.ofNullable(dto));

        Optional<ThresholdInfoDto> cultivationInfoDtoOptional = thresholdInfoService.findCultivationInfo(dto.getCultivationId());

        Assertions.assertTrue(cultivationInfoDtoOptional.isPresent());
        verify(thresholdInfoRepository, times(1)).findByCultivationId(anyLong());
    }

    @Test
    void test_findCultivationInfo_fail() {
        when(thresholdInfoRepository.findByCultivationId(anyLong())).thenReturn(Optional.empty());

        Optional<ThresholdInfoDto> cultivationInfoDtoOptional = thresholdInfoService.findCultivationInfo(dto.getCultivationId());

        Assertions.assertTrue(cultivationInfoDtoOptional.isEmpty());
        verify(thresholdInfoRepository, times(1)).findByCultivationId(anyLong());
    }
}
