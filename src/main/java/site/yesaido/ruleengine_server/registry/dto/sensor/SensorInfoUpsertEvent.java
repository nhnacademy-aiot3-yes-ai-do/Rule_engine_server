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
 * 센서 정보의 추가/수정을 위한 DTO입니다.
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