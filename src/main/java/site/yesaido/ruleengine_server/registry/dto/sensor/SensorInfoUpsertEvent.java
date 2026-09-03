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

    @NotNull
    private Long cultivationId;

    @NotBlank
    private String location;

    @NotBlank
    private String locationDetail;

    @NotBlank
    private String deviceModel;

    @NotBlank
    private String deviceName;

    @NotBlank
    private String deviceEui;

    @NotBlank
    private String sensorType;

    @NotBlank
    private String unit;

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