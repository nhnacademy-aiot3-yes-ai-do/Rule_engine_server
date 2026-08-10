package site.yesaido.ruleengine_server.registry.repository;

import site.yesaido.ruleengine_server.global.dto.ThresholdInfoDto;

import java.util.Optional;

/**
 * 재배 환경 정보(= 임계값)의 CRUD를 담당하는 Repository 인터페이스입니다.<br>
 * Redis 외 다른 Repository를 고려하여 인터페이스로 정의합니다.
 */
public interface ThresholdInfoRepository {

    /**
     * [Create & Update] 재배 환경 정보(= 임계값)를 삽입 또는 갱신합니다.
     * @param dto 삽입 또는 갱신할 재배 환경 정보를 담은 dto
     */
    void upsert(ThresholdInfoDto dto);

    /**
     * [Read] 재배 환경 정보(= 임계값)를 조회합니다.
     * @param cultivationId 조회할 재배 환경의 id
     * @return 조회된 재배 환경 정보(= 임계값) {@link ThresholdInfoDto}를 담은 {@link Optional}
     */
    Optional<ThresholdInfoDto> findByCultivationId(Long cultivationId);

    /**
     * [Delete] 재배 환경 정보(= 임계값)를 삭제합니다.
     * @param cultivationId 삭제할 재배 환경의 id
     */
    void deleteByCultivationId(Long cultivationId);

    // ==================================================

    /**
     * 재배 환경 정보(= 임계값)에 대한 존재 여부를 반환합니다.
     * @param cultivationId 존재 여부를 확인할 재배 환경의 id
     * @return {@code true} 재배 환경 정보(= 임계값)가 존재할 경우
     */
    boolean existsByCultivationId(Long cultivationId);
}
