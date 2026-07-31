package site.yesaido.ruleengine_server.global.exception;

import site.yesaido.ruleengine_server.collector.dto.SupportedTopic;

/**
 * 지원 대상 토픽 유형에 해당하지만, 토픽의 세부 세그먼트 수나 형식이 규격에 맞지 않을 때 발생하는 예외입니다.
 */
public class InvalidTopicFormatException extends RuntimeException {

    /**
     * {@link InvalidTopicFormatException}을 생성합니다.
     *
     * @param supportedTopic 처리 중이던 대상 지원 토픽 Enum
     * @param actualTopic    수신된 원본 토픽 문자열
     * @param reason         토픽 형식이 올바르지 않은 구체적 사유
     */
    public InvalidTopicFormatException(SupportedTopic supportedTopic, String actualTopic, String reason) {
        super("[%s] 토픽 형식이 올바르지 않습니다. (수신된 토픽: %s, 사유: %s)".formatted(
                supportedTopic.name(),
                actualTopic,
                reason
        ));
    }
}
