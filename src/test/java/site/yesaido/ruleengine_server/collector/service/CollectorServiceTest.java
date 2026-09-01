package site.yesaido.ruleengine_server.collector.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import site.yesaido.ruleengine_server.collector.dto.SensorDataDto;
import site.yesaido.ruleengine_server.collector.dto.SupportedTopic;
import site.yesaido.ruleengine_server.collector.event.SensorDataReadyEvent;
import site.yesaido.ruleengine_server.collector.exception.InvalidPayloadFormatException;
import site.yesaido.ruleengine_server.collector.exception.InvalidTopicFormatException;
import site.yesaido.ruleengine_server.collector.exception.UnsupportedTopicException;
import site.yesaido.ruleengine_server.collector.support.SensorDataParser;
import site.yesaido.ruleengine_server.collector.support.SensorDataValidator;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CollectorServiceTest {

    @Mock
    private SensorDataParser sensorDataParser;
    @Mock
    private SensorDataValidator sensorDataValidator;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    private CollectorService collectorService;

    @BeforeEach
    void setup() {
        collectorService = new CollectorService(List.of(sensorDataParser), sensorDataValidator, eventPublisher);
    }

    @Test
    void test_ingest_fail_throwUnsupportedTopicException() {
        when(sensorDataParser.supports(anyString())).thenReturn(false);

        Assertions.assertThrows(UnsupportedTopicException.class,
                () -> collectorService.ingest("unsupported/topic", "{}"));
    }

    @Test
    void test_ingest_fail_throwInvalidTopicFormatException() {
        when(sensorDataParser.supports(anyString())).thenReturn(true);
        when(sensorDataParser.parse(anyString(), anyString()))
                .thenThrow(new InvalidTopicFormatException(SupportedTopic.MUSHROOM, "토픽 요소 개수가 올바르지 않습니다.", "mushroom/a/b/c/d"));

        collectorService.ingest("mushroom/a/b/c/d", "{}");

        verify(sensorDataValidator, never()).validate(any(SensorDataDto.class));
    }

    @Test
    void test_ingest_fail_throwInvalidPayloadFormatException() {
        when(sensorDataParser.supports(anyString())).thenReturn(true);
        when(sensorDataParser.parse(anyString(), anyString()))
                .thenThrow(new InvalidPayloadFormatException(SupportedTopic.MUSHROOM, "mushroom/a/b/c/d/e", "페이로드가 올바르지 않습니다."));

        collectorService.ingest("mushroom/a/b/c/d/e", "{}");

        verify(sensorDataValidator, never()).validate(any(SensorDataDto.class));
    }

    @Test
    void test_ingest_fail_validateFail() {
        String topic = "mushroom/a/b/c/d/TEMPERATURE";
        String payload = "{}";
        SensorDataDto dto = mock(SensorDataDto.class);

        when(sensorDataParser.supports(topic)).thenReturn(true);
        when(sensorDataParser.parse(topic, payload)).thenReturn(List.of(dto));
        when(sensorDataValidator.validate(dto)).thenReturn(Optional.empty());

        collectorService.ingest(topic, payload);

        verifyNoInteractions(eventPublisher);
    }

    @Test
    void test_ingest_success() {
        String topic = "mushroom/a/b/c/d/TEMPERATURE";
        String payload = "{}";
        SensorDataDto dto = mock(SensorDataDto.class);

        when(sensorDataParser.supports(topic)).thenReturn(true);
        when(sensorDataParser.parse(topic, payload)).thenReturn(List.of(dto));
        when(sensorDataValidator.validate(dto)).thenReturn(Optional.of(dto));

        collectorService.ingest(topic, payload);

        verify(eventPublisher, times(1)).publishEvent(any(SensorDataReadyEvent.class));
    }
}
