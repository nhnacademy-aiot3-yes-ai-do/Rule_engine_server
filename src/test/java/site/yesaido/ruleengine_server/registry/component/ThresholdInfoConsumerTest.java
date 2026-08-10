package site.yesaido.ruleengine_server.registry.component;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.yesaido.ruleengine_server.registry.dto.threshold.ThresholdInfoEvent;
import site.yesaido.ruleengine_server.registry.service.ThresholdInfoService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ThresholdInfoConsumerTest {

    @Mock
    private ThresholdInfoService thresholdInfoService;

    @InjectMocks
    private ThresholdInfoConsumer thresholdInfoConsumer;

    @Test
    void test_consumeCultivationInfoUpsert() {

        ThresholdInfoEvent event = mock(ThresholdInfoEvent.class);

        thresholdInfoConsumer.consumeThresholdInfoEvent(event);

        verify(thresholdInfoService, times(1))
                .processThresholdInfoEvent(any(ThresholdInfoEvent.class));
    }

}
