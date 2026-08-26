package site.yesaido.ruleengine_server.engine.dto.actuator;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ActuatorCommandStatus {

    APPLIED("[Success] Actuator 제어 명령에 성공했습니다."),
    REJECTED_EXPIRED("[Fail] 명령 유효 시간이 만료되어 제어 요청이 거절되었습니다."),
    REJECTED_STALE("[Fail] 동일한 명령이 이미 적용되어 있어 제어 요청이 거절되었습니다."),
    REJECTED_CONFLICT("[Fail] 반대 방향 Actuator가 이미 동작 중이어서 제어 요청이 거절되었습니다.");

    private final String message;
}
