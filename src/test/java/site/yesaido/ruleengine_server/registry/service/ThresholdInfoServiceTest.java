package site.yesaido.ruleengine_server.registry.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.yesaido.ruleengine_server.global.dto.ThresholdInfoDto;
import site.yesaido.ruleengine_server.registry.dto.threshold.SensorRange;
import site.yesaido.ruleengine_server.registry.dto.threshold.ThresholdInfoEvent;
import site.yesaido.ruleengine_server.registry.repository.ThresholdInfoRepository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
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

    @Mock
    private ManagedSensorTypeService managedSensorTypeService;

    @InjectMocks
    private ThresholdInfoService thresholdInfoService;

    private ThresholdInfoEvent event;
    private List<SensorRange> sensorRangeList;
    private ThresholdInfoDto dto;

    @BeforeEach
    void setUp() {
        sensorRangeList = new ArrayList<>();
        sensorRangeList.add(new SensorRange("TEMPERATURE", "°C", BigDecimal.valueOf(20.0), BigDecimal.valueOf(30.0)));
        sensorRangeList.add(new SensorRange("HUMIDITY", "%", BigDecimal.valueOf(60.0), BigDecimal.valueOf(80.0)));
        sensorRangeList.add(new SensorRange("CO2", "ppm", BigDecimal.valueOf(600.0), BigDecimal.valueOf(800.0)));
        sensorRangeList.add(new SensorRange("LIGHT", "lx", BigDecimal.valueOf(0.0), BigDecimal.valueOf(500.0)));
        event = new ThresholdInfoEvent(
                1L,
                sensorRangeList
        );

        dto = ThresholdInfoDto.from(event.getCultivationId(), event.getSensorRangeList());
    }

    @Test
    void test_processThresholdInfoEvent_whenNoExistingRecord_createsNew() {
        when(thresholdInfoRepository.findByCultivationId(anyLong())).thenReturn(Optional.empty());

        thresholdInfoService.processThresholdInfoEvent(event); // setUp()의 4개짜리 event

        verify(thresholdInfoRepository, times(1)).findByCultivationId(anyLong());
        verify(thresholdInfoRepository, times(1)).upsert(any(ThresholdInfoDto.class));
        verify(managedSensorTypeService, times(1)).registerAll(anyList());
        verify(thresholdInfoRepository, never()).deleteByCultivationId(anyLong());
    }

    @Test
    void test_processThresholdInfoEvent_whenExistingRecordFound_mergesIntoIt() {
        SensorRange rangeToUpdate = new SensorRange("TEMPERATURE", "°C", BigDecimal.valueOf(20.0), BigDecimal.valueOf(30.0));
        ThresholdInfoEvent updateEvent = new ThresholdInfoEvent(1L, List.of(rangeToUpdate), OffsetDateTime.now());
        ThresholdInfoDto existingDto = mock(ThresholdInfoDto.class);
        when(thresholdInfoRepository.findByCultivationId(anyLong())).thenReturn(Optional.of(existingDto));

        thresholdInfoService.processThresholdInfoEvent(updateEvent);

        verify(thresholdInfoRepository, times(1)).findByCultivationId(anyLong());
        verify(existingDto, times(1)).applyRange(rangeToUpdate);
        verify(thresholdInfoRepository, times(1)).upsert(any(ThresholdInfoDto.class));
        verify(managedSensorTypeService, times(1)).registerAll(anyList());
        verify(thresholdInfoRepository, never()).deleteByCultivationId(anyLong());
    }

    @Test
    void test_processThresholdInfoEvent_whenSingleSensorAndNoExistingRecord_createsInsteadOfThrowing() {
        SensorRange rangeToCreate = new SensorRange("TEMPERATURE", "°C", BigDecimal.valueOf(20.0), BigDecimal.valueOf(30.0));
        ThresholdInfoEvent createEvent = new ThresholdInfoEvent(1L, List.of(rangeToCreate));
        when(thresholdInfoRepository.findByCultivationId(anyLong())).thenReturn(Optional.empty());

        thresholdInfoService.processThresholdInfoEvent(createEvent);

        verify(thresholdInfoRepository, times(1)).findByCultivationId(anyLong());
        verify(thresholdInfoRepository, times(1)).upsert(any(ThresholdInfoDto.class));
        verify(managedSensorTypeService, times(1)).registerAll(anyList());
        verify(thresholdInfoRepository, never()).deleteByCultivationId(anyLong());
    }

    @Test
    void test_processThresholdInfoEvent_whenSensorRangeListSizeIsTwo_noLongerSilentlyIgnored() {
        List<SensorRange> twoRanges = List.of(
                new SensorRange("TEMPERATURE", "°C", BigDecimal.valueOf(20.0), BigDecimal.valueOf(30.0)),
                new SensorRange("HUMIDITY", "%", BigDecimal.valueOf(60.0), BigDecimal.valueOf(80.0))
        );
        ThresholdInfoEvent twoRangeEvent = new ThresholdInfoEvent(1L, twoRanges);
        when(thresholdInfoRepository.findByCultivationId(anyLong())).thenReturn(Optional.empty());

        thresholdInfoService.processThresholdInfoEvent(twoRangeEvent);

        verify(thresholdInfoRepository, times(1)).upsert(any(ThresholdInfoDto.class));
        verify(managedSensorTypeService, times(1)).registerAll(anyList());
    }

    @Test
    void test_processThresholdInfoEvent_whenSensorRangeListSizeIsZero() {
        ThresholdInfoEvent deleteEvent = new ThresholdInfoEvent(1L, Collections.emptyList());

        thresholdInfoService.processThresholdInfoEvent(deleteEvent);

        verify(thresholdInfoRepository, times(1)).deleteByCultivationId(anyLong());
        verify(thresholdInfoRepository, never()).upsert(any(ThresholdInfoDto.class));
        verify(thresholdInfoRepository, never()).findByCultivationId(anyLong());
        verify(managedSensorTypeService, never()).registerAll(anyList());
    }

    @Test
    void test_processThresholdInfoEvent_whenSensorRangeDuplicate_throwException() {

        sensorRangeList.add(new SensorRange("TEMPERATURE", "°C", BigDecimal.valueOf(20.0), BigDecimal.valueOf(30.0)));

        Assertions.assertThrows(IllegalArgumentException.class, () -> thresholdInfoService.processThresholdInfoEvent(event));
    }

    @Test
    void test_findCultivationInfo() {

        when(thresholdInfoRepository.findByCultivationId(anyLong())).thenReturn(Optional.of(dto));

        Optional<ThresholdInfoDto> thresholdInfoDtoOptional = thresholdInfoService.findCultivationInfo(1L);

        Assertions.assertTrue(thresholdInfoDtoOptional.isPresent());
        Assertions.assertEquals(dto, thresholdInfoDtoOptional.get());

        verify(thresholdInfoRepository, times(1)).findByCultivationId(anyLong());
    }
}
