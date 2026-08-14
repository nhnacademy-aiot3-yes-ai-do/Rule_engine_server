package site.yesaido.ruleengine_server.global.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import site.yesaido.ruleengine_server.registry.dto.sensor.SensorInfoUpsertEvent;

/**
 * 센서 정보를 담고 있는 DTO입니다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SensorInfoDto {

    /**
     * 센서가 속한 재배 환경의 식별자(ID)
     */
    @NotNull
    private Long cultivationId;

    /**
     * 센서 설치 대분류 위치 (예: 하우스A)
     */
    @NotBlank
    private String location;

    /**
     * 센서 설치 상세 위치 (예: 1번 베드)
     */
    @NotBlank
    private String locationDetail;

    /**
     * 센서 디바이스 모델명
     */
    @NotBlank
    private String deviceModel;

    /**
     * 센서 디바이스 이름
     */
    @NotBlank
    private String deviceName;

    /**
     * 센서 디바이스의 EUI (고유 식별자)
     */
    @NotBlank
    private String deviceEui;

    /**
     * 센서 종류 (예: TEMPERATURE, HUMIDITY 등)
     */
    @NotNull
    private String sensorType;

    /**
     * 센서 측정 단위 (예: °C, %, ppm 등)
     */
    @NotNull
    private String unit;

    /**
     * {@link SensorInfoUpsertEvent}로부터 {@link SensorInfoDto}를 생성하는 정적 팩토리 메서드입니다.
     *
     * @param event 센서 정보 생성/갱신 이벤트
     * @return 매핑된 SensorInfoDto 인스턴스
     */
    public static SensorInfoDto from(SensorInfoUpsertEvent event) {
        SensorInfoDto dto = new SensorInfoDto();

        dto.cultivationId = event.getCultivationId();
        dto.location = event.getLocation();
        dto.locationDetail = event.getLocationDetail();
        dto.deviceModel = event.getDeviceModel();
        dto.deviceName = event.getDeviceName();
        dto.deviceEui = event.getDeviceEui();
        dto.sensorType = event.getSensorType();
        dto.unit = event.getUnit();

        return dto;
    }
}