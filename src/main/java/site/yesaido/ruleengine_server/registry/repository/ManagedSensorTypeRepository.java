package site.yesaido.ruleengine_server.registry.repository;

import java.util.Set;

/**
 * 센서 타입 목록을 관리하는 Repository 인터페이스입니다.<br>
 * 이 목록에 없는 sensorType의 데이터는 Collector 단계에서 미등록 데이터로 폐기됩니다.<br>
 * ThresholdInfoEvent를 통해 새로운 sensorType이 들어오면 동적으로 등록됩니다.
 */
public interface ManagedSensorTypeRepository {

    /**
     * sensorType을 관리 목록에 등록합니다. <br>
     * 이미 등록된 sensorType이면 아무 동작도 하지 않습니다.
     *
     * @param sensorType 등록할 센서 타입
     */
    void register(String sensorType);

    /**
     * 해당 sensorType이 관리 목록에 등록되어 있는지 확인합니다.
     *
     * @param sensorType 확인할 센서 타입
     * @return {@code true} 등록되어 있는 경우, 그렇지 않으면 {@code false}
     */
    boolean isManaged(String sensorType);

    /**
     * 현재 관리 중인 모든 sensorType 목록을 조회합니다.
     *
     * @return 관리 중인 센서 타입의 집합
     */
    Set<String> findAll();
}
