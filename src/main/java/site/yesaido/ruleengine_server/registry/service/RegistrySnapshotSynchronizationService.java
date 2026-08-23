package site.yesaido.ruleengine_server.registry.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import site.yesaido.ruleengine_server.global.dto.SensorInfoDto;
import site.yesaido.ruleengine_server.global.dto.ThresholdInfoDto;
import site.yesaido.ruleengine_server.registry.client.CultivationSnapshotClient;
import site.yesaido.ruleengine_server.registry.dto.snapshot.CultivationSensorResponse;
import site.yesaido.ruleengine_server.registry.dto.snapshot.CultivationSensorTypeResponse;
import site.yesaido.ruleengine_server.registry.dto.snapshot.CultivationSnapshotResponse;
import site.yesaido.ruleengine_server.registry.dto.snapshot.CultivationThresholdResponse;
import site.yesaido.ruleengine_server.registry.dto.threshold.SensorRange;
import site.yesaido.ruleengine_server.registry.exception.RegistrySnapshotSynchronizationException;
import site.yesaido.ruleengine_server.registry.repository.SensorInfoRepository;
import site.yesaido.ruleengine_server.registry.repository.ThresholdInfoRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 컨테이너 시작 시 Cultivation Service의 전체 센서/임계값 snapshot으로
 * SensorInfo/ThresholdInfo Redis 캐시를 채웁니다.
 * RabbitMQ 이벤트는 "지금 이 순간부터"의 변경만 반영하므로,
 * 이 프로세스가 뜨기 전에 이미 등록된 센서는 이 초기 동기화 없이는 영원히 알 수 없습니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RegistrySnapshotSynchronizationService {

    private final CultivationSnapshotClient cultivationSnapshotClient;
    private final SensorInfoRepository sensorInfoRepository;
    private final ThresholdInfoRepository thresholdInfoRepository;
    private final ManagedSensorTypeService managedSensorTypeService;

    public void synchronizeAll() {
        try {
            CultivationSnapshotResponse snapshot = cultivationSnapshotClient.getSnapshot();

            if (snapshot == null || snapshot.snapshotAt() == null) {
                throw new RegistrySnapshotSynchronizationException(
                        "Cultivation Service의 snapshot 응답이 올바르지 않습니다.");
            }

            int sensorCount = synchronizeSensors(snapshot.sensors());
            int thresholdCount = synchronizeThresholds(snapshot.thresholds());

            log.info("Cultivation Service snapshot으로 초기 동기화를 완료했습니다. "
                            + "snapshotAt={}, sensorChannelCount={}, thresholdCultivationCount={}",
                    snapshot.snapshotAt(), sensorCount, thresholdCount);
        } catch (RegistrySnapshotSynchronizationException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new RegistrySnapshotSynchronizationException(
                    "Cultivation Service snapshot 동기화에 실패했습니다.", exception);
        }
    }

    private int synchronizeSensors(List<CultivationSensorResponse> sensors) {
        if (sensors == null) {
            return 0;
        }

        int count = 0;
        for (CultivationSensorResponse sensor : sensors) {
            if (sensor.sensorTypes() == null) {
                continue;
            }
            for (CultivationSensorTypeResponse sensorType : sensor.sensorTypes()) {
                SensorInfoDto dto = new SensorInfoDto(
                        sensor.cultivationId(),
                        sensor.location(),
                        sensor.locationDetail(),
                        sensor.deviceModel(),
                        sensor.deviceName(),
                        sensor.deviceEui(),
                        sensorType.sensorType(),
                        sensorType.unit()
                );
                sensorInfoRepository.upsert(dto);
                count++;
            }
        }
        return count;
    }

    private int synchronizeThresholds(List<CultivationThresholdResponse> thresholds) {
        if (thresholds == null) {
            return 0;
        }

        Map<Long, List<CultivationThresholdResponse>> byCultivationId = thresholds.stream()
                .collect(Collectors.groupingBy(CultivationThresholdResponse::cultivationId));

        for (Map.Entry<Long, List<CultivationThresholdResponse>> entry : byCultivationId.entrySet()) {
            List<SensorRange> ranges = entry.getValue().stream()
                    .map(t -> new SensorRange(t.sensorType(), t.unit(), t.minValue(), t.maxValue()))
                    .toList();

            ThresholdInfoDto dto = ThresholdInfoDto.from(entry.getKey(), ranges);
            thresholdInfoRepository.upsert(dto);

            managedSensorTypeService.registerAll(
                    ranges.stream().map(SensorRange::getSensorType).toList()
            );
        }

        return byCultivationId.size();
    }
}