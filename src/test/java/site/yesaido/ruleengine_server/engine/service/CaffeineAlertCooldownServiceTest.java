package site.yesaido.ruleengine_server.engine.service;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import site.yesaido.ruleengine_server.engine.dto.SensorKey;
import site.yesaido.ruleengine_server.engine.service.impl.CaffeineAlertCooldownService;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CaffeineAlertCooldownServiceTest {

    private static final long DEFAULT_STATE_TTL_HOURS = 24;

    private SensorKey sensorKey;

    @BeforeEach
    void setUp() {
        sensorKey = new SensorKey("24e124128c067999", "TEMPERATURE", "°C");
    }

    @Test
    void test_tryMarkExceeded_whenFirstTime_returnsTrue() {
        CaffeineAlertCooldownService service = new CaffeineAlertCooldownService(10, DEFAULT_STATE_TTL_HOURS);

        assertTrue(service.tryMarkExceeded(sensorKey));
    }

    @Test
    void test_tryMarkExceeded_whenAlreadyInCooldown_returnsFalse() {
        CaffeineAlertCooldownService service = new CaffeineAlertCooldownService(10, DEFAULT_STATE_TTL_HOURS);

        service.tryMarkExceeded(sensorKey);

        assertFalse(service.tryMarkExceeded(sensorKey));
    }

    @Test
    void test_tryMarkExceeded_afterCooldownExpires_returnsTrueAgain() {
        // 쿨다운 0분으로 설정 시 Caffeine이 즉시(또는 매우 짧게) 만료시키는지 확인
        CaffeineAlertCooldownService service = new CaffeineAlertCooldownService(0, DEFAULT_STATE_TTL_HOURS);

        service.tryMarkExceeded(sensorKey);

        Awaitility.await()
                .atMost(Duration.ofSeconds(2))
                .untilAsserted(() -> assertTrue(service.tryMarkExceeded(sensorKey)));
    }

    @Test
    void test_tryMarkNormal_whenNeverExceeded_returnsFalse() {
        CaffeineAlertCooldownService service = new CaffeineAlertCooldownService(10, DEFAULT_STATE_TTL_HOURS);

        assertFalse(service.tryMarkNormal(sensorKey));
    }

    @Test
    void test_tryMarkNormal_afterExceeded_returnsTrue() {
        CaffeineAlertCooldownService service = new CaffeineAlertCooldownService(10, DEFAULT_STATE_TTL_HOURS);

        service.tryMarkExceeded(sensorKey);

        assertTrue(service.tryMarkNormal(sensorKey));
    }

    @Test
    void test_tryMarkNormal_whenCalledTwice_secondCallReturnsFalse() {
        CaffeineAlertCooldownService service = new CaffeineAlertCooldownService(10, DEFAULT_STATE_TTL_HOURS);

        service.tryMarkExceeded(sensorKey);
        service.tryMarkNormal(sensorKey);

        assertFalse(service.tryMarkNormal(sensorKey));
    }

    @Test
    void test_tryMarkNormal_resetsCooldown_allowingImmediateReExceed() {
        // 핵심 회귀 테스트: 복귀 시 쿨다운이 리셋되어, 쿨다운 시간이 안 지났어도 재초과 시 알림이 나가야 함
        CaffeineAlertCooldownService service = new CaffeineAlertCooldownService(10, DEFAULT_STATE_TTL_HOURS);

        service.tryMarkExceeded(sensorKey);   // [00:00] 초과 -> true
        service.tryMarkNormal(sensorKey);     // [01:00] 복귀 -> true, 쿨다운 리셋

        // [01:30] 재초과 - 10분 쿨다운이 안 지났어도 리셋됐으니 true여야 함
        assertTrue(service.tryMarkExceeded(sensorKey));
    }

    @Test
    void test_tryMarkExceeded_afterStateTtlExpires_treatsAsNewOccurrence() {
        // state TTL이 지나면 오래 방치된 초과 상태가 정리되어, 다음 tryMarkExceeded가 새로운 발생으로 취급되는지 검증
        CaffeineAlertCooldownService service = new CaffeineAlertCooldownService(10, 0);

        service.tryMarkExceeded(sensorKey);

        Awaitility.await()
                .atMost(Duration.ofSeconds(2))
                .untilAsserted(() -> assertFalse(service.tryMarkNormal(sensorKey)));
    }
}