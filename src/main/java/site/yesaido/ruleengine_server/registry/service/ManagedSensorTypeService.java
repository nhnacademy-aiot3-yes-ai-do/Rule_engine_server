package site.yesaido.ruleengine_server.registry.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import site.yesaido.ruleengine_server.registry.repository.ManagedSensorTypeRepository;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 관리 대상 센서 타입({@code sensorType}) 목록을 메모리 캐시 및 저장소에 관리하는 서비스 클래스입니다.<br>
 * 수집기(Collector)에서 유효한 센서 데이터인지 빠르게 검증할 수 있도록 인메모리 캐시를 제공합니다.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ManagedSensorTypeService {

    private final ManagedSensorTypeRepository managedSensorTypeRepository;

    /**
     * 관리 대상 센서 타입 목록을 보관하는 인메모리 캐시 (Thread-safe)
     */
    private final Set<String> cache = ConcurrentHashMap.newKeySet();

    /**
     * 애플리케이션 시작 시 저장소에 보관된 모든 센서 타입을 조회하여 인메모리 캐시에 적재합니다.
     */
    @PostConstruct
    void loadCache() {
        cache.addAll(managedSensorTypeRepository.findAll());
        log.info("[ManagedSensorTypeService] 관리 중인 센서 타입 로드: {}", cache);
    }

    /**
     * 특정 센서 타입을 저장소 및 인메모리 캐시에 등록합니다.
     *
     * @param sensorType 등록할 센서 타입
     */
    public void register(String sensorType) {
        managedSensorTypeRepository.register(sensorType);
        cache.add(sensorType);
    }

    /**
     * 여러 센서 타입 목록을 저장소 및 인메모리 캐시에 일괄 등록합니다.
     *
     * @param sensorTypes 등록할 센서 타입 목록
     */
    public void registerAll(List<String> sensorTypes) {
        sensorTypes.forEach(managedSensorTypeRepository::register);
        cache.addAll(sensorTypes);
    }

    /**
     * 주어진 센서 타입이 현재 관리 대상에 포함되어 있는지 인메모리 캐시를 통해 확인합니다.
     *
     * @param sensorType 확인할 센서 타입
     * @return {@code true} 관리 대상 센서 타입인 경우, 그렇지 않으면 {@code false}
     */
    public boolean isManaged(String sensorType) {
        return cache.contains(sensorType);
    }

    /**
     * 현재 관리 중인 모든 센서 타입 목록의 불변 복사본을 반환합니다.
     *
     * @return 관리 중인 센서 타입 집합(Set)
     */
    public Set<String> findAll() {
        return Set.copyOf(cache);
    }
}
