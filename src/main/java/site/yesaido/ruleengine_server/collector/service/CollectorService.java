package site.yesaido.ruleengine_server.collector.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import site.yesaido.ruleengine_server.collector.dto.SensorDataDto;
import site.yesaido.ruleengine_server.collector.event.SensorDataReadyEvent;
import site.yesaido.ruleengine_server.collector.exception.InvalidPayloadFormatException;
import site.yesaido.ruleengine_server.collector.exception.InvalidTopicFormatException;
import site.yesaido.ruleengine_server.collector.exception.UnsupportedTopicException;
import site.yesaido.ruleengine_server.collector.support.SensorDataParser;
import site.yesaido.ruleengine_server.collector.support.SensorDataValidator;

import java.util.List;

/**
 * MQTT 구독을 통해 수신 받은 메세지를 파싱/검증하여 RuleEngine에게 넘기는 기능을 담당하는 <strong>CollectorService</strong>입니다.<br>
 * 1. MQTT 구독 중 메세지가 들어오면 {@code collectorService.ingest(topic, payload)}를 호출하여, CollectorService에게 topic과 payload를 넘깁니다.<br>
 * 2. 수신된 topic을 지원하는 {@link SensorDataParser}를 찾아 payload를 {@link SensorDataDto} 목록을 파싱합니다.<br>
 * 3. 파싱된 DTO는 {@link SensorDataValidator}를 통해 유효성을 검증받고, 검증을 통과한 데이터는 이벤트를 발행하여 RuleEngine으로 전달됩니다.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class CollectorService {

    private final List<SensorDataParser> sensorDataParserList;
    private final SensorDataValidator sensorDataValidator;

    private final ApplicationEventPublisher eventPublisher;

    /**
     * MQTT 구독을 통해 수신한 메세지를 처리하는 진입점입니다.
     * @param topic 수신된 토픽
     * @param payload 수신된 페이로드
     */
    public void ingest(String topic, String payload) {

        SensorDataParser parser = sensorDataParserList.stream()
                .filter(p -> p.supports(topic))
                .findFirst()
                .orElseThrow(() -> new UnsupportedTopicException(topic));

        List<SensorDataDto> dtoList;
        try {
            dtoList = parser.parse(topic, payload);

        } catch (InvalidTopicFormatException | InvalidPayloadFormatException e) {
            log.warn("파싱 실패 - 폐기: {}", e.getMessage());
            return;
        } catch (Exception e) {
            log.error("예상치 못한 오류 파싱 에러", e);
            return;
        }
        dtoList.forEach(this::validateAndPublish);
    }

    /**
     * 파싱된 센서 데이터 DTO의 유효성을 검증하고, 검증 성공 시 후속 이벤트를 발행합니다.
     * @param sensorDataDto 검증 및 발행 대상에 해당하는 센서 데이터 DTO
     */
    private void validateAndPublish(SensorDataDto sensorDataDto) {

        sensorDataValidator.validate(sensorDataDto)
                .ifPresentOrElse(
                        sensorData -> eventPublisher.publishEvent(new SensorDataReadyEvent(sensorData)),
                        () -> log.warn("[Collector - Validation] 검증 실패: {}", sensorDataDto)
                );
    }
}
