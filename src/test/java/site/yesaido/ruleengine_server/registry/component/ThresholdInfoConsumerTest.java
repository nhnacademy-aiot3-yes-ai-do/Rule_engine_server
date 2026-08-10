package site.yesaido.ruleengine_server.registry.component;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.yesaido.ruleengine_server.registry.dto.threshold.ThresholdInfoDeleteEvent;
import site.yesaido.ruleengine_server.registry.dto.threshold.ThresholdInfoEvent;
import site.yesaido.ruleengine_server.registry.service.ThresholdInfoService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ThresholdInfoConsumerTest {

    @Mock
    private ThresholdInfoService thresholdInfoService;

    @InjectMocks
    private ThresholdInfoConsumer thresholdInfoConsumer;

    @Test
    void test_consumeCultivationInfoUpsert() {
        ThresholdInfoEvent dto = new ThresholdInfoEvent(
                1L,
                20.0, 30.0,
                60.0, 80.0,
                600.0, 800.0,
                0.0, 500.0
        );

        thresholdInfoConsumer.consumeCultivationInfoUpsert(dto);

        verify(thresholdInfoService, times(1))
                .upsertCultivationInfo(any(ThresholdInfoEvent.class));
    }

    @Test
    void test_consumeCultivationInfoDelete() {
        ThresholdInfoDeleteEvent deleteDto = new ThresholdInfoDeleteEvent();
        deleteDto.setCultivationId(1L);

        thresholdInfoConsumer.consumeCultivationInfoDelete(deleteDto);

        verify(thresholdInfoService, times(1))
                .deleteCultivationInfo(any(ThresholdInfoDeleteEvent.class));
    }

}
