package site.yesaido.ruleengine_server.registry.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import site.yesaido.ruleengine_server.registry.repository.ManagedSensorTypeRepository;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RequiredArgsConstructor
@Service
public class ManagedSensorTypeService {

    private final ManagedSensorTypeRepository managedSensorTypeRepository;

    private final Set<String> cache = ConcurrentHashMap.newKeySet();

    @PostConstruct
    void loadCache() {
        cache.addAll(managedSensorTypeRepository.findAll());
        log.info("[ManagedSensorTypeService] 관리 중인 센서 타입 로드: {}", cache);
    }

    public void register(String sensorType) {
        managedSensorTypeRepository.register(sensorType);
        cache.add(sensorType);
    }

    public void registerAll(List<String> sensorTypes) {
        sensorTypes.forEach(managedSensorTypeRepository::register);
        cache.addAll(sensorTypes);
    }

    public boolean isManaged(String sensorType) {
        return cache.contains(sensorType);
    }

    public Set<String> findAll() {
        return Set.copyOf(cache);
    }
}
