package site.yesaido.ruleengine_server.collector.support;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import site.yesaido.ruleengine_server.collector.dto.SensorDataDto;
import site.yesaido.ruleengine_server.global.dto.SensorInfoDto;
import site.yesaido.ruleengine_server.registry.service.ManagedSensorTypeService;
import site.yesaido.ruleengine_server.registry.service.SensorInfoService;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * 파싱이 완료된 센서 데이터 {@link SensorDataDto}의 검증을 담당하는 클래스입니다.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class SensorDataValidator {

    private final SensorInfoService sensorInfoService;
    private final ManagedSensorTypeService managedSensorTypeService;

    /**
     * 파싱이 완료된 센서 데이터 {@link SensorDataDto}에 대한 검증을 수행하는 메서드입니다.<br>
     * - 등록되어 있는 센서인지 확인합니다.<br>
     * - 물리적으로 유효한 범위인지 확인합니다.<br>
     * - 검증을 통과한 경우 {@code cultivationId}를 채워 넣습니다.
     * @param sensorDataDto 검증 대상에 해당하는 센서 데이터
     * @return {@code true} 검증 대상 센서 데이터가 검증을 통과한 경우
     */
    public boolean isValid(SensorDataDto sensorDataDto) {

        String deviceEui = sensorDataDto.getDeviceEui();
        String sensorType = sensorDataDto.getSensorType();
        BigDecimal value = sensorDataDto.getValue();
        String unit = sensorDataDto.getUnit();

        if (!managedSensorTypeService.isManaged(sensorType)) {
            log.warn("미관리 센서 타입 데이터 폐기: deviceEui={}, sensorType={}", deviceEui, sensorType);
            return false;
        }

        if (value == null) {
            log.warn("센서 값이 null인 데이터 폐기: deviceEui={}, sensorType={}", deviceEui, sensorType);
            return false;
        }

        Optional<SensorInfoDto> optionalSensorInfoDto = sensorInfoService.findSensorInfo(deviceEui, sensorType, unit);

        if (optionalSensorInfoDto.isEmpty()) {
            log.warn("미등록 센서 데이터 폐기: deviceEui={}, sensorType={}", deviceEui, sensorType);
            return false;
        }

        sensorDataDto.setCultivationId(optionalSensorInfoDto.get().getCultivationId());

        return true;
    }

}
