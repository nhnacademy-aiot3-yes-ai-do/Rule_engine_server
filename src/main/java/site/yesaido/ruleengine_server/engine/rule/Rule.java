package site.yesaido.ruleengine_server.engine.rule;

import site.yesaido.ruleengine_server.collector.dto.SensorDataDto;
import site.yesaido.ruleengine_server.engine.core.RuleEngine;
import site.yesaido.ruleengine_server.global.dto.ThresholdInfoDto;

/**
 * {@link RuleEngine}에서 센서 데이터를 바탕으로 적용시킬 Rule에 대한 인터페이스입니다.<br>
 * 구현체는 특정 센서 타입이나 조건에 맞춰 규칙 적용 가능 여부를 판단하고,
 * 실제 수치 명가 및 알림/이벤트 발행 등의 판단 로직을 수행합니다.
 */
public interface Rule {

    /**
     * 전달된 센서 데이터가 이 규칙을 적용할 대상인지 여부를 판단합니다.
     *
     * @param dto 수신된 센서 데이터 DTO
     * @return 이 규칙을 적용할 수 있는 경우 {@code true}, 그렇지 않은 경우 {@code false}
     */
    boolean supports(SensorDataDto dto);

    /**
     * 임계값 정보를 바탕으로 센서 데이터의 실제 조건 판단 로직을 수행합니다.
     *
     * @param sensorData    수신된 센서 데이터 DTO
     * @param thresholdInfo 해당 재배지에 설정된 임계값 정보 DTO
     */
    void evaluate(SensorDataDto sensorData, ThresholdInfoDto thresholdInfo);
}
