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

/**
 * 센서 정보({@link SensorInfoDto})의 등록, 수정, 조회, 삭제 비즈니스 로직을 담당하는 서비스 클래스입니다.<br>
 * 센서 정보 이벤트를 전달받아 저장소({@link SensorInfoRepository})에 반영하고 관련 상태를 관리합니다.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class SensorInfoService {

    private final ThresholdInfoRepository thresholdInfoRepository;
    private final SensorInfoRepository sensorInfoRepository;

    /**
     * 센서 정보 생성 및 갱신 이벤트({@link SensorInfoUpsertEvent})를 받아 센서 정보를 등록 또는 수정합니다.
     * <p>
     * 해당 센서가 속한 재배 환경(임계값 정보)의 존재 여부를 확인하며,
     * 재배 환경 정보가 아직 도착하지 않은 경우 경고 로그를 남긴 후 센서 정보를 저장합니다.
     * </p>
     *
     * @param sensorInfoUpsertEvent 등록 또는 갱신할 센서 정보 이벤트 DTO
     */
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

    /**
     * 디바이스 EUI, 센서 타입, 측정 단위를 기반으로 센서 정보를 조회합니다.
     *
     * @param deviceEui  조회할 센서의 Device EUI
     * @param sensorType 조회할 센서 종류
     * @param unit       조회할 센서의 측정 단위
     * @return 조회된 센서 정보 {@link SensorInfoDto}를 담은 {@link Optional} (존재하지 않는 경우 빈 Optional)
     */
    public Optional<SensorInfoDto> findSensorInfo(String deviceEui, String sensorType, String unit) {
        return sensorInfoRepository.findByDeviceEuiAndSensorType(deviceEui, sensorType, unit);
    }

    /**
     * 센서 정보 삭제 이벤트({@link SensorInfoDeleteEvent})를 받아 센서 정보를 삭제합니다.
     * <p>
     * 삭제 대상 센서 정보가 저장소에 존재하지 않는 경우 경고 로그를 남긴 후 삭제를 수행합니다.
     * </p>
     *
     * @param sensorInfoDeleteEvent 삭제할 센서 정보 이벤트 DTO
     */
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
