package site.yesaido.ruleengine_server.registry.component;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;
import site.yesaido.ruleengine_server.registry.exception.RegistrySnapshotSynchronizationException;
import site.yesaido.ruleengine_server.registry.service.RegistrySnapshotSynchronizationService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrySnapshotSynchronizationInitializerTest {

    @Mock
    private RegistrySnapshotSynchronizationService synchronizationService;

    @Mock
    private ApplicationArguments applicationArguments;

    @Test
    @DisplayName("첫 시도에 성공하면 재시도/대기 없이 바로 끝난다")
    void test_run_success_onFirstAttempt() {
        RegistrySnapshotSynchronizationInitializer initializer =
                new RegistrySnapshotSynchronizationInitializer(synchronizationService);

        assertDoesNotThrow(() -> initializer.run(applicationArguments));

        verify(synchronizationService, times(1)).synchronizeAll();
    }

    @Test
    @DisplayName("몇 번 실패하다가 재시도 끝에 성공하면 정상 종료한다")
    void test_run_retriesAndSucceeds() {
        RegistrySnapshotSynchronizationException failure =
                new RegistrySnapshotSynchronizationException("동기화 실패");

        doThrow(failure)
                .doThrow(failure)
                .doNothing()
                .when(synchronizationService).synchronizeAll();

        RegistrySnapshotSynchronizationInitializer initializer =
                spy(new RegistrySnapshotSynchronizationInitializer(synchronizationService));
        doReturn(true).when(initializer).sleep(anyLong());

        assertDoesNotThrow(() -> initializer.run(applicationArguments));

        verify(synchronizationService, times(3)).synchronizeAll();
    }

    @Test
    @DisplayName("최대 재시도 횟수(8회)를 모두 소진하면 마지막 예외를 그대로 다시 던진다")
    void test_run_exhaustsRetries_thenRethrowsOriginalException() {
        RegistrySnapshotSynchronizationException failure =
                new RegistrySnapshotSynchronizationException("계속 실패");
        doThrow(failure).when(synchronizationService).synchronizeAll();

        RegistrySnapshotSynchronizationInitializer initializer =
                spy(new RegistrySnapshotSynchronizationInitializer(synchronizationService));
        doReturn(true).when(initializer).sleep(anyLong());

        RegistrySnapshotSynchronizationException thrown = assertThrows(
                RegistrySnapshotSynchronizationException.class,
                () -> initializer.run(applicationArguments)
        );

        assertSame(failure, thrown);
        verify(synchronizationService, times(8)).synchronizeAll();
    }

    @Test
    @DisplayName("대기 중 인터럽트되면 남은 재시도를 소진하지 않고 즉시 중단한다")
    void test_run_interruptedDuringBackoff_abortsImmediately() {
        RegistrySnapshotSynchronizationException failure =
                new RegistrySnapshotSynchronizationException("실패");
        doThrow(failure).when(synchronizationService).synchronizeAll();

        RegistrySnapshotSynchronizationInitializer initializer =
                spy(new RegistrySnapshotSynchronizationInitializer(synchronizationService));
        doReturn(false).when(initializer).sleep(anyLong());

        RegistrySnapshotSynchronizationException thrown = assertThrows(
                RegistrySnapshotSynchronizationException.class,
                () -> initializer.run(applicationArguments)
        );

        assertSame(failure, thrown);
        verify(synchronizationService, times(1)).synchronizeAll();
    }

    @Test
    @DisplayName("sleep()은 정상적으로 대기하면 true를 반환한다")
    void test_sleep_normalCompletion_returnsTrue() {
        RegistrySnapshotSynchronizationInitializer initializer =
                new RegistrySnapshotSynchronizationInitializer(synchronizationService);

        assertTrue(initializer.sleep(1));
    }

    @Test
    @DisplayName("sleep() 대기 중 인터럽트되면 false를 반환하고 인터럽트 상태를 복원한다")
    void test_sleep_interrupted_returnsFalseAndRestoresInterruptFlag() {
        RegistrySnapshotSynchronizationInitializer initializer =
                new RegistrySnapshotSynchronizationInitializer(synchronizationService);

        Thread.currentThread().interrupt();
        try {
            assertFalse(initializer.sleep(50));
            assertTrue(Thread.currentThread().isInterrupted());
        } finally {
            Thread.interrupted(); // 다른 테스트 오염 방지
        }
    }
}