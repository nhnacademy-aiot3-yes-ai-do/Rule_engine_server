package site.yesaido.ruleengine_server.registry.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.yesaido.ruleengine_server.global.dto.SensorInfoDto;
import site.yesaido.ruleengine_server.global.dto.ThresholdInfoDto;
import site.yesaido.ruleengine_server.registry.client.CultivationSnapshotClient;
import site.yesaido.ruleengine_server.registry.dto.snapshot.CultivationSensorResponse;
import site.yesaido.ruleengine_server.registry.dto.snapshot.CultivationSensorTypeResponse;
import site.yesaido.ruleengine_server.registry.dto.snapshot.CultivationSnapshotResponse;
import site.yesaido.ruleengine_server.registry.dto.snapshot.CultivationThresholdResponse;
import site.yesaido.ruleengine_server.registry.exception.RegistrySnapshotSynchronizationException;
import site.yesaido.ruleengine_server.registry.repository.SensorInfoRepository;
import site.yesaido.ruleengine_server.registry.repository.ThresholdInfoRepository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrySnapshotSynchronizationServiceTest {

    @Mock
    private CultivationSnapshotClient cultivationSnapshotClient;

    @Mock
    private SensorInfoRepository sensorInfoRepository;

    @Mock
    private ThresholdInfoRepository thresholdInfoRepository;

    @Mock
    private ManagedSensorTypeService managedSensorTypeService;

    @InjectMocks
    private RegistrySnapshotSynchronizationService registrySnapshotSynchronizationService;

    @Test
    @DisplayName("센서/임계값 snapshot을 정상적으로 Redis에 반영한다")
    void test_synchronizeAll_success() {
        CultivationSensorResponse sensorA = new CultivationSensorResponse(
                1L, "device-eui-1", "device-name-1", "장소", "위치상세", "model-1",
                List.of(
                        new CultivationSensorTypeResponse("TEMPERATURE", "°C"),
                        new CultivationSensorTypeResponse("HUMIDITY", "%")
                )
        );
        CultivationSensorResponse sensorB = new CultivationSensorResponse(
                2L, "device-eui-2", "device-name-2", "장소2", "위치상세2", "model-2",
                List.of(new CultivationSensorTypeResponse("CO2", "ppm"))
        );

        CultivationThresholdResponse thresholdA1 = new CultivationThresholdResponse(
                1L, "TEMPERATURE", "°C", BigDecimal.valueOf(15), BigDecimal.valueOf(25));
        CultivationThresholdResponse thresholdA2 = new CultivationThresholdResponse(
                1L, "HUMIDITY", "%", BigDecimal.valueOf(40), BigDecimal.valueOf(70));
        CultivationThresholdResponse thresholdB1 = new CultivationThresholdResponse(
                2L, "CO2", "ppm", BigDecimal.valueOf(400), BigDecimal.valueOf(1200));

        CultivationSnapshotResponse snapshot = new CultivationSnapshotResponse(
                OffsetDateTime.now(),
                List.of(sensorA, sensorB),
                List.of(thresholdA1, thresholdA2, thresholdB1)
        );

        when(cultivationSnapshotClient.getSnapshot()).thenReturn(snapshot);

        assertDoesNotThrow(() -> registrySnapshotSynchronizationService.synchronizeAll());

        // 센서: sensorA(2개 타입) + sensorB(1개 타입) = 3회 upsert
        verify(sensorInfoRepository, times(3)).upsert(any(SensorInfoDto.class));

        // 임계값: cultivationId 1, 2 → 2개 그룹 upsert
        ArgumentCaptor<ThresholdInfoDto> thresholdCaptor = ArgumentCaptor.forClass(ThresholdInfoDto.class);
        verify(thresholdInfoRepository, times(2)).upsert(thresholdCaptor.capture());

        List<ThresholdInfoDto> capturedThresholds = thresholdCaptor.getAllValues();
        assertTrue(capturedThresholds.stream().anyMatch(dto -> dto.getCultivationId().equals(1L)
                && dto.getRange("TEMPERATURE", "°C") != null
                && dto.getRange("HUMIDITY", "%") != null));
        assertTrue(capturedThresholds.stream().anyMatch(dto -> dto.getCultivationId().equals(2L)
                && dto.getRange("CO2", "ppm") != null));

        // 재배당 1번씩 registerAll 호출 (cultivationId 1, 2)
        verify(managedSensorTypeService, times(2)).registerAll(anyList());
    }

    @Test
    @DisplayName("snapshot 응답 자체가 null이면 동기화 예외를 던진다")
    void test_synchronizeAll_nullSnapshot_throwsException() {
        when(cultivationSnapshotClient.getSnapshot()).thenReturn(null);

        RegistrySnapshotSynchronizationException exception = assertThrows(
                RegistrySnapshotSynchronizationException.class,
                () -> registrySnapshotSynchronizationService.synchronizeAll()
        );

        assertTrue(exception.getMessage().contains("올바르지 않습니다"));
        verifyNoInteractions(sensorInfoRepository, thresholdInfoRepository, managedSensorTypeService);
    }

    @Test
    @DisplayName("snapshotAt이 null이면 동기화 예외를 던진다")
    void test_synchronizeAll_snapshotAtNull_throwsException() {
        CultivationSnapshotResponse snapshot = new CultivationSnapshotResponse(null, List.of(), List.of());
        when(cultivationSnapshotClient.getSnapshot()).thenReturn(snapshot);

        assertThrows(
                RegistrySnapshotSynchronizationException.class,
                () -> registrySnapshotSynchronizationService.synchronizeAll()
        );

        verifyNoInteractions(sensorInfoRepository, thresholdInfoRepository, managedSensorTypeService);
    }

    @Test
    @DisplayName("Feign 호출 자체가 실패하면 RegistrySnapshotSynchronizationException으로 감싸서 던진다")
    void test_synchronizeAll_clientThrowsRuntimeException_wrapsException() {
        RuntimeException feignFailure = new RuntimeException("Load balancer does not contain an instance");
        when(cultivationSnapshotClient.getSnapshot()).thenThrow(feignFailure);

        RegistrySnapshotSynchronizationException exception = assertThrows(
                RegistrySnapshotSynchronizationException.class,
                () -> registrySnapshotSynchronizationService.synchronizeAll()
        );

        assertEquals(feignFailure, exception.getCause());
        assertTrue(exception.getMessage().contains("동기화에 실패했습니다"));
    }

    @Test
    @DisplayName("sensors/thresholds가 null이어도 예외 없이 넘어가고 upsert를 호출하지 않는다")
    void test_synchronizeAll_nullSensorsAndThresholds_noUpsertCalled() {
        CultivationSnapshotResponse snapshot = new CultivationSnapshotResponse(
                OffsetDateTime.now(), null, null);
        when(cultivationSnapshotClient.getSnapshot()).thenReturn(snapshot);

        assertDoesNotThrow(() -> registrySnapshotSynchronizationService.synchronizeAll());

        verify(sensorInfoRepository, never()).upsert(any());
        verify(thresholdInfoRepository, never()).upsert(any());
        verify(managedSensorTypeService, never()).registerAll(anyList());
    }

    @Test
    @DisplayName("센서의 sensorTypes가 null이면 해당 센서는 건너뛴다")
    void test_synchronizeAll_sensorWithNullSensorTypes_skipped() {
        CultivationSensorResponse sensorWithNullTypes = new CultivationSensorResponse(
                1L, "device-eui-1", "device-name-1", "장소", "위치상세", "model-1", null);

        CultivationSnapshotResponse snapshot = new CultivationSnapshotResponse(
                OffsetDateTime.now(), List.of(sensorWithNullTypes), null);
        when(cultivationSnapshotClient.getSnapshot()).thenReturn(snapshot);

        assertDoesNotThrow(() -> registrySnapshotSynchronizationService.synchronizeAll());

        verify(sensorInfoRepository, never()).upsert(any());
    }
}