package site.yesaido.ruleengine_server.engine.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.yesaido.ruleengine_server.collector.dto.SensorDataDto;
import site.yesaido.ruleengine_server.engine.rule.Rule;
import site.yesaido.ruleengine_server.engine.service.InfluxService;
import site.yesaido.ruleengine_server.global.dto.SensorValueEvent;
import site.yesaido.ruleengine_server.global.dto.ThresholdInfoDto;
import site.yesaido.ruleengine_server.registry.service.ThresholdInfoService;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RuleEngineTest {

    @Mock
    private ThresholdInfoService thresholdInfoService;

    @Mock
    private InfluxService influxService;

    @Mock
    private Rule supportingRule;

    @Mock
    private Rule nonSupportingRule;

    private RuleEngine ruleEngine;

    private SensorDataDto dto;

    @BeforeEach
    void setUp() {
        ruleEngine = new RuleEngine(
                thresholdInfoService,
                influxService,
                List.of(supportingRule, nonSupportingRule)
        );

        dto = new SensorDataDto(
                "실습실", "후면 오른쪽",
                "AM107", "AM107-067999", "24e124128c067999",
                "TEMPERATURE",
                BigDecimal.valueOf(23.7),
                OffsetDateTime.now(),
                "°C",
                10L
        );
    }

    @Test
    void test_start_whenThresholdInfoNotFound_shouldNotPublishOrEvaluateRules() {
        when(thresholdInfoService.findCultivationInfo(anyLong())).thenReturn(Optional.empty());

        ruleEngine.start(dto);

        verify(thresholdInfoService, times(1)).findCultivationInfo(dto.getCultivationId());
        verify(influxService, never()).save(any(SensorValueEvent.class));
        verify(supportingRule, never()).supports(any());
        verify(nonSupportingRule, never()).supports(any());
    }

    @Test
    void test_start_whenThresholdInfoFound_shouldPublishAndEvaluateSupportedRules() {
        ThresholdInfoDto thresholdInfoDto = mock(ThresholdInfoDto.class);
        when(thresholdInfoService.findCultivationInfo(anyLong())).thenReturn(Optional.of(thresholdInfoDto));
        when(supportingRule.supports(dto)).thenReturn(true);
        when(nonSupportingRule.supports(dto)).thenReturn(false);

        ruleEngine.start(dto);

        verify(influxService, times(1)).save(any(SensorValueEvent.class));

        verify(supportingRule, times(1)).supports(dto);
        verify(supportingRule, times(1)).evaluate(dto, thresholdInfoDto);

        verify(nonSupportingRule, times(1)).supports(dto);
        verify(nonSupportingRule, never()).evaluate(any(), any());
    }

    @Test
    void test_start_whenThresholdInfoFound_shouldPublishRealTimeDataWithCorrectFields() {
        ThresholdInfoDto thresholdInfoDto = mock(ThresholdInfoDto.class);
        when(thresholdInfoService.findCultivationInfo(anyLong())).thenReturn(Optional.of(thresholdInfoDto));
        when(supportingRule.supports(dto)).thenReturn(false);
        when(nonSupportingRule.supports(dto)).thenReturn(false);

        ruleEngine.start(dto);

        org.mockito.ArgumentCaptor<SensorValueEvent> captor =
                org.mockito.ArgumentCaptor.forClass(SensorValueEvent.class);
        verify(influxService, times(1)).save(captor.capture());

        SensorValueEvent published = captor.getValue();
        org.assertj.core.api.Assertions.assertThat(published.deviceEui()).isEqualTo(dto.getDeviceEui());
        org.assertj.core.api.Assertions.assertThat(published.sensorType()).isEqualTo(dto.getSensorType());
        org.assertj.core.api.Assertions.assertThat(published.cultivationId()).isEqualTo(dto.getCultivationId());
    }
}