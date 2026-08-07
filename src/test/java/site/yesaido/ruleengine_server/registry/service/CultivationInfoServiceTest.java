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
import site.yesaido.ruleengine_server.registry.repository.CultivationInfoRepository;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CultivationInfoServiceTest {

    @Mock
    private CultivationInfoRepository cultivationInfoRepository;

    @InjectMocks
    private CultivationInfoService cultivationInfoService;

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
        cultivationInfoService.upsertCultivationInfo(dto);

        verify(cultivationInfoRepository, times(1))
                .upsertCultivationInfo(any(ThresholdInfoEvent.class));
    }

    @Test
    void test_findCultivationInfo_success() {
        when(cultivationInfoRepository.findCultivationInfo(anyLong())).thenReturn(Optional.ofNullable(dto));

        Optional<ThresholdInfoEvent> cultivationInfoDtoOptional = cultivationInfoService.findCultivationInfo(dto.getCultivationId());

        Assertions.assertTrue(cultivationInfoDtoOptional.isPresent());
        verify(cultivationInfoRepository, times(1)).findCultivationInfo(anyLong());
    }

    @Test
    void test_findCultivationInfo_fail() {
        when(cultivationInfoRepository.findCultivationInfo(anyLong())).thenReturn(Optional.empty());

        Optional<ThresholdInfoEvent> cultivationInfoDtoOptional = cultivationInfoService.findCultivationInfo(dto.getCultivationId());

        Assertions.assertTrue(cultivationInfoDtoOptional.isEmpty());
        verify(cultivationInfoRepository, times(1)).findCultivationInfo(anyLong());
    }

    @Test
    void test_deleteCultivationInfo() {

        when(cultivationInfoRepository.exists(anyLong())).thenReturn(true);

        ThresholdInfoDeleteEvent deleteDto = new ThresholdInfoDeleteEvent();
        deleteDto.setCultivationId(1L);
        cultivationInfoService.deleteCultivationInfo(deleteDto);

        verify(cultivationInfoRepository, times(1)).exists(anyLong());
        verify(cultivationInfoRepository, times(1)).deleteCultivationInfo(anyLong());
    }
}
