package site.yesaido.ruleengine_server.global.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 재배 환경 정보를 담고 있는 DTO입니다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CultivationInfoDto {

    @NotNull
    private Long cultivationId;

    // 온도 최솟값, 최댓값
    @NotNull
    private Double tempMin;
    @NotNull
    private Double tempMax;

    // 습도
    @NotNull
    private Double humidityMin;
    @NotNull
    private Double humidityMax;

    // CO2
    @NotNull
    private Double co2Min;
    @NotNull
    private Double co2Max;

    // 조도
    @NotNull
    private Double lightMin;
    @NotNull
    private Double lightMax;

}
