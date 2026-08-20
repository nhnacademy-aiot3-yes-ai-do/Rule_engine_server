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

    private SensorKey sensorKey;

    @BeforeEach
    void setUp() {
        sensorKey = new SensorKey("24e124128c067999", "TEMPERATURE", "°C");
    }

    @Test
    void test_canAlert_whenNeverRecorded_returnsTrue() {
        CaffeineAlertCooldownService service = new CaffeineAlertCooldownService(10);

        assertTrue(service.canAlert(sensorKey));
    }

    @Test
    void test_canAlert_afterRecordAlert_returnsFalse() {
        CaffeineAlertCooldownService service = new CaffeineAlertCooldownService(10);

        service.recordAlert(sensorKey);

        assertFalse(service.canAlert(sensorKey));
    }

    @Test
    void test_canAlert_afterCooldownExpires_returnsTrue() {
        // 쿨다운 0분으로 설정 시 Caffeine이 즉시(또는 매우 짧게) 만료시키는지 확인
        CaffeineAlertCooldownService service = new CaffeineAlertCooldownService(0);

        service.recordAlert(sensorKey);

        Awaitility.await()
                .atMost(Duration.ofSeconds(2))
                .untilAsserted(() -> assertTrue(service.canAlert(sensorKey)));
    }

    @Test
    void test_isCurrentlyExceeded_whenNeverMarked_returnsFalse() {
        CaffeineAlertCooldownService service = new CaffeineAlertCooldownService(10);

        assertFalse(service.isCurrentlyExceeded(sensorKey));
    }

    @Test
    void test_isCurrentlyExceeded_afterMarkExceeded_returnsTrue() {
        CaffeineAlertCooldownService service = new CaffeineAlertCooldownService(10);

        service.markExceeded(sensorKey);

        assertTrue(service.isCurrentlyExceeded(sensorKey));
    }

    @Test
    void test_isCurrentlyExceeded_afterMarkNormal_returnsFalse() {
        CaffeineAlertCooldownService service = new CaffeineAlertCooldownService(10);

        service.markExceeded(sensorKey);
        service.markNormal(sensorKey);

        assertFalse(service.isCurrentlyExceeded(sensorKey));
    }

    @Test
    void test_stateMap_survivesRegardlessOfCooldownExpiry() {
        // 핵심 회귀 테스트: 쿨다운(cache)이 만료돼도 상태(stateMap)는 별개로 유지되는지 검증
        CaffeineAlertCooldownService service = new CaffeineAlertCooldownService(0);

        service.recordAlert(sensorKey);
        service.markExceeded(sensorKey);

        Awaitility.await()
                .atMost(Duration.ofSeconds(2))
                .untilAsserted(() -> assertTrue(service.canAlert(sensorKey)));

        // 쿨다운은 만료됐지만, 상태는 여전히 "초과 중"으로 유지되어야 함
        assertTrue(service.isCurrentlyExceeded(sensorKey));
    }
}