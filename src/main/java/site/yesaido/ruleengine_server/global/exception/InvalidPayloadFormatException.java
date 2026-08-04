package site.yesaido.ruleengine_server.global.exception;

import site.yesaido.ruleengine_server.collector.dto.SupportedTopic;

/**
 * 수신한 메세지의 페이로드가 예상된 형식에 맞지 않아 파싱에 실패했을 때 발생하는 예외입니다.<br>
 * 필수 필드가 누락되었거나, JSON 문법 오류(괄호 누락, 오타로 인한 필드명 불일치 등)로
 * 페이로드를 대상 객체로 역직렬화할 수 없는 경우에 사용합니다.
 */
public class InvalidPayloadFormatException extends RuntimeException {

    /**
     * {@link InvalidPayloadFormatException}을 생성합니다.
     *
     * @param supportedTopic 처리 중이던 대상의 토픽 Enum
     * @param payload        수신한 페이로드
     * @param reason         페이로드 형식이 올바르지 않은 사유
     */
    public InvalidPayloadFormatException(SupportedTopic supportedTopic, String payload, String reason) {
        super("[%s] 페이로드 형식이 올바르지 않습니다. 사유=%s, 수신한 페이로드=%s".formatted(
                supportedTopic.name(),
                reason,
                payload
        ));
    }
}
