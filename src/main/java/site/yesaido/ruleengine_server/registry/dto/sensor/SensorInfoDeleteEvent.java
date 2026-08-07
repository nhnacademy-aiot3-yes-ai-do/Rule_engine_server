package site.yesaido.ruleengine_server.registry.dto.sensor;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 센서 정보 삭제을 위한 DTO입니다.
 */
@Getter
@Setter
@NoArgsConstructor
public class SensorInfoDeleteEvent {

    @NotNull
    private Long cultivationId;

    @NotNull
    private String deviceEui;
}