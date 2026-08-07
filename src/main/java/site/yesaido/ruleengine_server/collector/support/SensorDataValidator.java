package site.yesaido.ruleengine_server.collector.support;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import site.yesaido.ruleengine_server.collector.dto.SensorDataDto;
import site.yesaido.ruleengine_server.global.dto.SensorType;
import site.yesaido.ruleengine_server.global.dto.SensorInfoDto;
import site.yesaido.ruleengine_server.registry.service.SensorInfoService;

import java.util.Optional;

/**
 * 파싱이 완료된 센서 데이터 {@link SensorDataDto}의 검증을 담당하는 클래스입니다.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class SensorDataValidator {

    private static final double TEMPERATURE_MIN = -20.0;
    private static final double TEMPERATURE_MAX = 60.0;
    private static final double HUMIDITY_MIN = 0.0;
    private static final double HUMIDITY_MAX = 200.0;
    private static final double CO2_MIN = 0.0;
    private static final double CO2_MAX = 10000.0;
    private static final double LIGHT_MIN = 0.0;
    private static final double LIGHT_MAX = 1000.0;

    private final SensorInfoService sensorInfoService;

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
        SensorType sensorType = sensorDataDto.getSensorType();
        Double value = sensorDataDto.getValue();

        Optional<SensorInfoDto> optionalSensorInfoDto = sensorInfoService.findSensorInfo(deviceEui, sensorType);

        if (optionalSensorInfoDto.isEmpty()) {
            log.warn("미등록 센서 데이터 폐기: deviceEui={}, sensorType={}", deviceEui, sensorType.name());
            return false;
        }

        if (!isWithinPhysicalRange(sensorType, value)) {
            log.warn("물리적으로 유효하지 않은 값 폐기: deviceEui={}, sensorType={}, value={}", deviceEui, sensorType.name(), value);
            return false;
        }

        sensorDataDto.setCultivationId(optionalSensorInfoDto.get().getCultivationId());

        return true;
    }

    /**
     * 센서가 측정한 값이 물리적으로 유효한 범인지 확인하는 메서드입니다.
     * @param sensorType 센서 종류
     * @param value 센서가 측정한 값
     * @return {@code true} 센서가 측정한 값이 물리적으로 유효한 범위 내에 속한 경우
     */
    private boolean isWithinPhysicalRange(SensorType sensorType, Double value) {

        if (value == null) {
            return false;
        }

        return switch (sensorType) {
            case TEMPERATURE -> value >= TEMPERATURE_MIN && value <= TEMPERATURE_MAX;
            case HUMIDITY -> value >= HUMIDITY_MIN && value <= HUMIDITY_MAX;
            case CO2 -> value >= CO2_MIN && value <= CO2_MAX;
            case LIGHT -> value >= LIGHT_MIN && value <= LIGHT_MAX;
        };
    }
}
