package site.yesaido.ruleengine_server.registry.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.yesaido.ruleengine_server.registry.dto.threshold.ThresholdInfoDeleteEvent;
import site.yesaido.ruleengine_server.registry.dto.threshold.ThresholdInfoEvent;
import site.yesaido.ruleengine_server.registry.repository.ThresholdInfoRepository;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ThresholdInfoServiceTest {

    @Mock
    private ThresholdInfoRepository thresholdInfoRepository;

    @InjectMocks
    private ThresholdInfoService thresholdInfoService;

    private ThresholdInfoEvent dto;

    @BeforeEach
    void setUp() {
        dto = new ThresholdInfoEvent(
                1L,
                20.0, 30.0,
                60.0, 80.0,
                600.0, 800.0,
                0.0, 500.0
        );
    }

    @Test
    void test_upsertCultivationInfo() {
        thresholdInfoService.upsertCultivationInfo(dto);

        verify(thresholdInfoRepository, times(1))
                .upsert(any(ThresholdInfoEvent.class));
    }

    @Test
    void test_findCultivationInfo_success() {
        when(thresholdInfoRepository.findByCultivationId(anyLong())).thenReturn(Optional.ofNullable(dto));

        Optional<ThresholdInfoEvent> cultivationInfoDtoOptional = thresholdInfoService.findCultivationInfo(dto.getCultivationId());

        Assertions.assertTrue(cultivationInfoDtoOptional.isPresent());
        verify(thresholdInfoRepository, times(1)).findByCultivationId(anyLong());
    }

    @Test
    void test_findCultivationInfo_fail() {
        when(thresholdInfoRepository.findByCultivationId(anyLong())).thenReturn(Optional.empty());

        Optional<ThresholdInfoEvent> cultivationInfoDtoOptional = thresholdInfoService.findCultivationInfo(dto.getCultivationId());

        Assertions.assertTrue(cultivationInfoDtoOptional.isEmpty());
        verify(thresholdInfoRepository, times(1)).findByCultivationId(anyLong());
    }

    @Test
    void test_deleteCultivationInfo() {

        when(thresholdInfoRepository.existsByCultivationId(anyLong())).thenReturn(true);

        ThresholdInfoDeleteEvent deleteDto = new ThresholdInfoDeleteEvent();
        deleteDto.setCultivationId(1L);
        thresholdInfoService.deleteCultivationInfo(deleteDto);

        verify(thresholdInfoRepository, times(1)).existsByCultivationId(anyLong());
        verify(thresholdInfoRepository, times(1)).deleteByCultivationId(anyLong());
    }
}
