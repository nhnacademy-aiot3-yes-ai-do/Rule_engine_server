package site.yesaido.ruleengine_server.collector.exception;

import site.yesaido.ruleengine_server.collector.dto.SupportedTopic;

import java.util.Arrays;

/**
 * 수신된 MQTT 토픽이 시스템에서 처리 가능한 토픽 목록({@link SupportedTopic})에 포함되지 않을 때 발생하는 예외입니다.
 */
public class UnsupportedTopicException extends RuntimeException {

    /**
     * {@link UnsupportedTopicException}을 생성합니다.
     *
     * @param actualTopic 수신된 지원되지 않는 토픽 문자열
     */
    public UnsupportedTopicException(String actualTopic) {
        super("지원하지 않는 토픽입니다. (지원하는 토픽 목록: %s, 수신된 토픽: %s)".formatted(
                Arrays.toString(SupportedTopic.values()),
                actualTopic
        ));
    }
}
