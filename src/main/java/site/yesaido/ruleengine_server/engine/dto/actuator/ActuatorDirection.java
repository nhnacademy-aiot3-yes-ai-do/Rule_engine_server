package site.yesaido.ruleengine_server.engine.dto.actuator;

/**
 * 이 enum은 두 가지 의미라 사용됩니다.
 * <ul>
 *     <li>
 *         <b>판단 결과로 쓰일 때</b> (예: {@code determineDirection}의 반환값):
 *         지금 센서값을 기준으로 "앞으로 어느 방향이 필요한가"를 나타냅니다.
 *         이 경우 {@link #PENDING}은 나오지 않고, {@link #NONE}/{@link #INCREASING}/{@link #DECREASING} 중 하나만 반환됩니다.
 *     </li>
 *     <li>
 *         <b>저장된 상태로 쓰일 때</b> (예: {@code ActuatorControlState.direction}):
 *         지금 우리가 마지막으로 알고 있는 "실제 액추에이터의 동작 상태"를 나타냅니다.
 *         이 경우 위 세 가지에 더해 {@link #PENDING}(명령을 보냈지만 아직 응답을 확인하지 못한 상태)까지 나올 수 있습니다.
 *     </li>
 * </ul>
 */
public enum ActuatorDirection {

    /**
     * [판단] 값을 올리는 방향의 조치가 필요함<br>
     * [상태] 값을 올리는 액추에이터가 동작 중임
     */
    INCREASING,

    /**
     * [판단] 값을 내리는 방향의 조치가 필요함<br>
     * [상태] 값을 내리는 액추에이터가 동작 중임
     */
    DECREASING,

    /**
     * [판단] 조치가 필요 없음(정상 범위)<br>
     * [상태] 어떤 액추에이터도 동작하고 있지 않음
     */
    NONE,

    /**
     * [상태 전용] 명령은 보냈지만 아직 응답(성공/실패)을 확인하지 못한 상태.<br>
     * 판단 결과로는 절대 나오지 않습니다 — 센서값만으로 "요청 중"이라는 결론이 날 수는 없기 때문입니다.
     */
    PENDING
}
