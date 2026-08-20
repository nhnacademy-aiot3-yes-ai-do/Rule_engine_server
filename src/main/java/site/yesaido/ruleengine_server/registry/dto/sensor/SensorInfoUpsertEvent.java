package site.yesaido.ruleengine_server.registry.dto.sensor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * 센서 정보의 추가 및 수정을 위한 이벤트 DTO입니다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SensorInfoUpsertEvent {

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
    @NotBlank
    private String sensorType;

    /**
     * 센서 측정 단위 (예: °C, %, ppm 등)
     */
    @NotBlank
    private String unit;

    /**
     * 이벤트 발생 시각
     */
    @NotNull
    OffsetDateTime occurredAt;

    /**
     * 발생 시각을 현재 시각(KST, UTC+9)으로 자동 설정하는 생성자입니다.
     *
     * @param cultivationId  재배 환경 ID
     * @param location       설치 위치
     * @param locationDetail 설치 상세 위치
     * @param deviceModel    디바이스 모델명
     * @param deviceName     디바이스 이름
     * @param deviceEui      디바이스 EUI
     * @param sensorType     센서 종류
     * @param unit           측정 단위
     */
    public SensorInfoUpsertEvent(Long cultivationId, String location, String locationDetail, String deviceModel, String deviceName, String deviceEui, String sensorType, String unit) {
        this.cultivationId = cultivationId;
        this.location = location;
        this.locationDetail = locationDetail;
        this.deviceModel = deviceModel;
        this.deviceName = deviceName;
        this.deviceEui = deviceEui;
        this.sensorType = sensorType;
        this.unit = unit;
        this.occurredAt = OffsetDateTime.now(ZoneOffset.UTC).withOffsetSameInstant(ZoneOffset.ofHours(9));
    }
}