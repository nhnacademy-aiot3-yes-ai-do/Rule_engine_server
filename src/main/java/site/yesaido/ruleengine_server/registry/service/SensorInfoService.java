package site.yesaido.ruleengine_server.registry.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import site.yesaido.ruleengine_server.registry.dto.sensor.SensorInfoDeleteEvent;
import site.yesaido.ruleengine_server.global.dto.SensorInfoDto;
import site.yesaido.ruleengine_server.registry.dto.sensor.SensorInfoUpsertEvent;
import site.yesaido.ruleengine_server.registry.repository.ThresholdInfoRepository;
import site.yesaido.ruleengine_server.registry.repository.SensorInfoRepository;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class SensorInfoService {

    private final ThresholdInfoRepository thresholdInfoRepository;
    private final SensorInfoRepository sensorInfoRepository;

    public void upsertSensorInfo(SensorInfoUpsertEvent sensorInfoUpsertEvent) {

        SensorInfoDto sensorInfoDto = SensorInfoDto.from(sensorInfoUpsertEvent);

        if (!thresholdInfoRepository.existsByCultivationId(sensorInfoDto.getCultivationId())) {
            /* 재배 환경(= 임계값) 정보와 센서 정보를 전달해주는 쪽에서는 [재배 환경 추가] -> [센서 추가] 순서로 요청을 보냅니다.
               혹시 모를 지연이 생겨 재배 환경 정보보다 센서 정보가 먼저 도착할 경우를 고려하여 로그만 남기며, 센서 추가 요청을 막지는 않습니다. */
            log.warn("센서(deviceModel={}, deviceEui={}, sensorType={})가 추가될 재배 환경((cultivationId={})이 존재하지 않음",
                    sensorInfoDto.getDeviceModel(),
                    sensorInfoDto.getDeviceEui(),
                    sensorInfoDto.getSensorType(),
                    sensorInfoDto.getCultivationId()
            );
        }

        sensorInfoRepository.upsert(sensorInfoDto);

        log.debug("sensorInfo 적제: cultivationId={}, sensorType={}", sensorInfoDto.getCultivationId(), sensorInfoDto.getSensorType());
    }

    public Optional<SensorInfoDto> findSensorInfo(String deviceEui, String sensorType, String unit) {
        return sensorInfoRepository.findByDeviceEuiAndSensorType(deviceEui, sensorType, unit);
    }

    public void deleteSensorInfo(SensorInfoDeleteEvent sensorInfoDeleteEvent) {

        long cultivationId = sensorInfoDeleteEvent.getCultivationId();
        String deviceEui = sensorInfoDeleteEvent.getDeviceEui();
        String sensorType = sensorInfoDeleteEvent.getSensorType();
        String unit = sensorInfoDeleteEvent.getUnit();

        if (!sensorInfoRepository.existsByDeviceEuiAndSensorType(deviceEui, sensorType, unit)) {
            log.warn("삭제하려는 센서(cultivationId={}, deviceEui={}, sensorType={}, unit={})가 존재하지 않음", cultivationId, deviceEui, sensorType, unit);
        }

        sensorInfoRepository.deleteByDeviceEuiAndSensorType(
                deviceEui,
                sensorType,
                unit
        );

        log.debug("sensorInfo 삭제: cultivationId={}, deviceEui={}, sensorType={}, unit={}", cultivationId, deviceEui, sensorType, unit);
    }
}
