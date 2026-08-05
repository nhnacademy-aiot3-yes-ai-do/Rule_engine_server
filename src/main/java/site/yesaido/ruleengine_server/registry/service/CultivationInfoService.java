package site.yesaido.ruleengine_server.registry.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import site.yesaido.ruleengine_server.registry.dto.cultivation.CultivationInfoDeleteDto;
import site.yesaido.ruleengine_server.registry.dto.cultivation.CultivationInfoDto;
import site.yesaido.ruleengine_server.registry.repository.CultivationInfoRepository;

@Slf4j
@RequiredArgsConstructor
@Service
public class CultivationInfoService {

    private final CultivationInfoRepository cultivationInfoRepository;

    public void upsertCultivationInfo(CultivationInfoDto cultivationInfoDto) {
        cultivationInfoRepository.upsertCultivationInfo(
                cultivationInfoDto
        );

        log.debug("cultivationInfo 적제: cultivationId={}", cultivationInfoDto.getCultivationId());
    }

    public void deleteCultivationInfo(CultivationInfoDeleteDto cultivationInfoDeleteDto) {

        long cultivationId = cultivationInfoDeleteDto.getCultivationId();

        if (!cultivationInfoRepository.exists(cultivationId)) {
            log.warn("삭제하려는 재배 환경(cultivationId={})이 존재하지 않음 (이미 삭제되었거나 동기화 전)", cultivationId);
        }

        cultivationInfoRepository.deleteCultivationInfo(
                cultivationId
        );

        log.debug("cultivationInfo 삭제: cultivationId={}", cultivationId);
    }

}
