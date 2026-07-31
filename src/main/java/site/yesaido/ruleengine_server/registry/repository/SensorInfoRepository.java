package site.yesaido.ruleengine_server.registry.repository;

import site.yesaido.ruleengine_server.global.dto.SensorType;
import site.yesaido.ruleengine_server.registry.dto.sensor.SensorInfoDto;

import java.util.Optional;

/**
 * 센서 정보의 CRUD를 담당하는 Repository 인터페이스입니다.<br>
 * Redis 외 다른 Repository를 고려하여 인터페이스로 정의합니다.
 */
public interface SensorInfoRepository {

    /**
     * [Create & Update] 센서 정보를 삽입 또는 갱신합니다.
     * @param dto 삽입 또는 갱신할 센서에 대한 정보를 담은 dto
     */
    void upsertSensorInfo(SensorInfoDto dto);

    /**
     * [Read] 센서 정보를 조회합니다.
     * @param deviceEui 조회할 센서의 deviceEui
     * @param sensorType 조회할 센서 종류
     * @return 조회된 센서 정보 {@link SensorInfoDto}를 담은 {@link  Optional} (존재하지 않은 경우 빈 Optional)
     */
    Optional<SensorInfoDto> findSensorInfo(String deviceEui, SensorType sensorType);

    /**
     * [Delete] 센서 정보를 삭제합니다.
     * @param deviceEui 삭제할 센서의 deviceEui
     * @param sensorType 삭제할 센서 종류
     */
    void deleteSensorInfo(String deviceEui, SensorType sensorType);

    // ==================================================

    /**
     * 센서 정보에 대한 존재 여부를 반환합니다.
     * @param deviceEui 삭제할 센서의 deviceEui
     * @param sensorType 존재 여부를 확인할 센서 종류
     * @return {@code true} 센서 정보가 존재할 경우
     */
    boolean exists(String deviceEui, SensorType sensorType);
}
