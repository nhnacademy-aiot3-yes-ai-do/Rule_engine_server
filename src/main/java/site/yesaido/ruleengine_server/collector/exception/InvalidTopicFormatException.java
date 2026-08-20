package site.yesaido.ruleengine_server.collector.exception;

import site.yesaido.ruleengine_server.collector.dto.SupportedTopic;

/**
 * 지원 대상 토픽 유형에 해당하지만, 토픽의 세부 세그먼트 수나 형식이 규격에 맞지 않을 때 발생하는 예외입니다.
 */
public class InvalidTopicFormatException extends RuntimeException {

    /**
     * {@link InvalidTopicFormatException}을 생성합니다.
     *
     * @param supportedTopic 처리 중이던 대상 토픽 Enum
     * @param reason         토픽 형식이 올바르지 않은 사유
     * @param actualTopic    수신된 원본 토픽 문자열
     */
    public InvalidTopicFormatException(SupportedTopic supportedTopic, String reason, String actualTopic) {
        super("[%s] 토픽 형식이 올바르지 않습니다. 사유=%s, 수신된 토픽=%s".formatted(
                supportedTopic.name(),
                reason,
                actualTopic
        ));
    }
}
