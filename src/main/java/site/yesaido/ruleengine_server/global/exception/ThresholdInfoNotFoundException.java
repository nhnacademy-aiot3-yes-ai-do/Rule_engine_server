package site.yesaido.ruleengine_server.global.exception;

/**
 * 임계값 정보를 찾을 수 없는 경우 발생하는 예외입니다.
 */
public class ThresholdInfoNotFoundException extends RuntimeException {

    /**
     * {@link ThresholdInfoNotFoundException}을 생성합니다.
     * @param cultivationId 조회를 시도한 재배 환경 id
     */
    public ThresholdInfoNotFoundException(Long cultivationId) {
        super("임계값 정보를 찾을 수 없습니다. cultivationId: %d".formatted(cultivationId));
    }
}
