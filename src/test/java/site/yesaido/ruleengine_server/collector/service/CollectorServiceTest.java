package site.yesaido.ruleengine_server.collector.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.yesaido.ruleengine_server.collector.dto.SensorDataDto;
import site.yesaido.ruleengine_server.collector.dto.SupportedTopic;
import site.yesaido.ruleengine_server.collector.support.SensorDataParser;
import site.yesaido.ruleengine_server.collector.support.SensorDataValidator;
import site.yesaido.ruleengine_server.global.exception.InvalidPayloadFormatException;
import site.yesaido.ruleengine_server.global.exception.InvalidTopicFormatException;
import site.yesaido.ruleengine_server.global.exception.UnsupportedTopicException;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CollectorServiceTest {

    @Mock
    private SensorDataParser sensorDataParser;
    @Mock
    private SensorDataValidator sensorDataValidator;

    private CollectorService collectorService;

    @BeforeEach
    void setup() {
        collectorService = new CollectorService(List.of(sensorDataParser), sensorDataValidator);
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

        verify(sensorDataValidator, never()).isValid(any(SensorDataDto.class));
    }

    @Test
    void test_ingest_fail_throwInvalidPayloadFormatException() {
        when(sensorDataParser.supports(anyString())).thenReturn(true);
        when(sensorDataParser.parse(anyString(), anyString()))
                .thenThrow(new InvalidPayloadFormatException(SupportedTopic.MUSHROOM, "mushroom/a/b/c/d/e", "페이로드가 올바르지 않습니다."));

        collectorService.ingest("mushroom/a/b/c/d/e", "{}");

        verify(sensorDataValidator, never()).isValid(any(SensorDataDto.class));
    }

    // ApplicationEventPublisher 적용된 이후 테스트 추가 작성 예정
}
