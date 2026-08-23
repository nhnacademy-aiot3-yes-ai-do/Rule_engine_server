package site.yesaido.ruleengine_server.registry.component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import site.yesaido.ruleengine_server.registry.exception.RegistrySnapshotSynchronizationException;
import site.yesaido.ruleengine_server.registry.service.RegistrySnapshotSynchronizationService;

/**
 * 컨테이너 시작(=context refresh) 완료 직후 Cultivation Service의 snapshot으로
 * SensorInfo/ThresholdInfo Redis 캐시를 채웁니다.
 * <p>
 * SmartLifecycle의 이른 phase로 실행하면 Eureka discovery 캐시가 아직 준비되지 않아
 * "No servers available for service: cultivation-server"로 실패하므로,
 * data_generator의 CultivationSensorSynchronizationInitializer와 동일하게
 * ApplicationRunner(= context refresh 완료 후 실행)를 사용합니다.
 */
@Slf4j
@Component
@Profile("!local")
@RequiredArgsConstructor
public class RegistrySnapshotSynchronizationInitializer implements ApplicationRunner {

    private static final int MAX_ATTEMPTS = 8;
    private static final long INITIAL_BACKOFF_MILLISECONDS = 1000L;
    private static final long MAX_BACKOFF_MILLISECONDS = 15000L;
    private static final double BACKOFF_MULTIPLIER = 2.0;

    private final RegistrySnapshotSynchronizationService synchronizationService;

    @Override
    public void run(ApplicationArguments args) {
        synchronizeWithRetry();
    }

    private void synchronizeWithRetry() {
        int attempt = 1;
        long backoffMilliseconds = INITIAL_BACKOFF_MILLISECONDS;

        while (true) {
            try {
                synchronizationService.synchronizeAll();
                log.info("초기 registry snapshot 동기화를 완료했습니다. attempt={}", attempt);
                return;
            } catch (RegistrySnapshotSynchronizationException exception) {
                if (attempt >= MAX_ATTEMPTS) {
                    log.error("초기 registry snapshot 동기화가 최대 시도 횟수를 초과했습니다. maxAttempts={}. "
                                    + "동기화 없이는 기존 센서 데이터가 계속 누락되므로 애플리케이션 시작을 실패시킵니다.",
                            MAX_ATTEMPTS, exception);
                    throw exception;
                }

                log.warn("초기 registry snapshot 동기화에 실패해 재시도합니다. "
                                + "attempt={}, nextAttempt={}, backoffMilliseconds={}",
                        attempt, attempt + 1, backoffMilliseconds, exception);

                if (!sleep(backoffMilliseconds)) {
                    log.warn("초기 registry snapshot 동기화 재시도 대기 중 인터럽트가 발생해 재시도를 중단합니다. attempt={}", attempt);
                    throw exception;
                }

                backoffMilliseconds = Math.min(
                        (long) (backoffMilliseconds * BACKOFF_MULTIPLIER), MAX_BACKOFF_MILLISECONDS);
                attempt++;
            }
        }
    }

    protected boolean sleep(long millis) {
        try {
            Thread.sleep(millis);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}