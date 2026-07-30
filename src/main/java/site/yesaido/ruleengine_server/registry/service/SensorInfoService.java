package site.yesaido.ruleengine_server.registry.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import site.yesaido.ruleengine_server.global.dto.SensorType;
import site.yesaido.ruleengine_server.registry.dto.sensor.SensorInfoDeleteDto;
import site.yesaido.ruleengine_server.registry.dto.sensor.SensorInfoDto;
import site.yesaido.ruleengine_server.registry.repository.CultivationInfoRepository;
import site.yesaido.ruleengine_server.registry.repository.SensorInfoRepository;

@Slf4j
@RequiredArgsConstructor
@Service
public class SensorInfoService {

    private final CultivationInfoRepository cultivationInfoRepository;
    private final SensorInfoRepository sensorInfoRepository;

    public void upsertSensorInfo(SensorInfoDto sensorInfoDto) {

        if (!cultivationInfoRepository.exists(sensorInfoDto.getCultivationId())) {
            log.warn("센서(deviceModel={}, deviceEui={}, sensorType={})가 추가될 재배 환경((cultivationId={})이 존재하지 않음",
                    sensorInfoDto.getDeviceModel(),
                    sensorInfoDto.getDeviceEui(),
                    sensorInfoDto.getSensorType().name(),
                    sensorInfoDto.getCultivationId()
            );
        }

        sensorInfoRepository.upsertSensorInfo(
                sensorInfoDto
        );

        log.info("sensorInfo 적제: cultivationId={}, sensorType={}", sensorInfoDto.getCultivationId(), sensorInfoDto.getSensorType().name());
    }

    public void deleteSensorInfo(SensorInfoDeleteDto sensorInfoDeleteDto) {

        long cultivationId = sensorInfoDeleteDto.getCultivationId();
        String deviceEui = sensorInfoDeleteDto.getDeviceEui();
        SensorType sensorType = sensorInfoDeleteDto.getSensorType();

        if (!sensorInfoRepository.exists(deviceEui, sensorType)) {
            log.warn("삭제하려는 센서(cultivationId={}, deviceEui={}, sensorType={})가 존재하지 않음", cultivationId, deviceEui, sensorType.name());
        }

        sensorInfoRepository.deleteSensorInfo(
                deviceEui,
                sensorType
        );

        log.info("sensorInfo 삭제: cultivationId={}, deviceEui={}, sensorType={}", cultivationId, deviceEui, sensorType.name());
    }
}
