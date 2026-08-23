package site.yesaido.ruleengine_server.global.dto;

import site.yesaido.ruleengine_server.collector.dto.SensorDataDto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * InfluxDB 저장 및 후속 처리를 위해 정규화된 센서 측정값 이벤트 Record입니다.
 *
 * @param place         설치 구역 (예: 온실 등)
 * @param location      설치 대분류 위치 (예: 하우스A)
 * @param deviceModel   디바이스 모델명
 * @param deviceName    디바이스 이름
 * @param deviceEui     디바이스 EUI (고유 식별자)
 * @param sensorType    센서 종류
 * @param unit          측정 단위
 * @param value         센서 측정값
 * @param time          측정 시각
 * @param cultivationId 재배 환경 식별자(ID)
 */
public record SensorValueEvent(
        String place,
        String location,
        String deviceModel,
        String deviceName,
        String deviceEui,
        String sensorType,
        String unit,
        BigDecimal value,
        OffsetDateTime time,
        Long cultivationId
) {
    /**
     * {@link SensorDataDto}로부터 {@link SensorValueEvent}를 생성하는 정적 팩토리 메서드입니다.
     *
     * @param dto 수집 및 검증된 센서 데이터 DTO
     * @return 매핑된 SensorValueEvent 인스턴스
     */
    public static SensorValueEvent from(SensorDataDto dto) {
        return new SensorValueEvent(
                dto.getPlace(),
                dto.getLocation(),
                dto.getDeviceModel(),
                dto.getDeviceName(),
                dto.getDeviceEui(),
                dto.getSensorType(),
                dto.getUnit(),
                dto.getValue(),
                dto.getTime(),
                dto.getCultivationId()
        );
    }
}