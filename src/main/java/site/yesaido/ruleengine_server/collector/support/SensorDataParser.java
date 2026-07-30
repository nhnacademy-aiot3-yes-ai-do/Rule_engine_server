package site.yesaido.ruleengine_server.collector.support;

import site.yesaido.ruleengine_server.collector.dto.SensorDataDto;

import java.util.List;

/**
 * MQTT 구독을 통해 수신한 메세지의 파싱을 담당하는 Parser 인터페이스입니다.<br>
 * 토픽별로 구현체를 나누기 위해 인터페이스로 정의합니다. (application/#, mushroom/#, ...)
 */
public interface SensorDataParser {

    /**
     * 이 파서가 해당 토픽을 처리할 수 있는 여부를 반환합니다.
     * @param topic 수신한 메세지의 토픽
     * @return {@code true} 해당 토픽을 이 파서가 처리할 수 있는 경우
     */
    boolean supports(String topic);

    /**
     * 토픽과 페이로드를 파싱하여 SensorDataDto 목록을 반환합니다.
     * @param topic 수신한 메세지의 토픽
     * @param payload 수신한 메세지의 페이로드
     * @return 파싱된 SensorDataDto 목록 (하나의 메세지에서 여러 센서 타입의 데이터가 파싱될 수 있습니다.)
     */
    List<SensorDataDto> parse(String topic, String payload);
}
