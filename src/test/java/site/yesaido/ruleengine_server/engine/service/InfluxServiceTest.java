package site.yesaido.ruleengine_server.engine.service;

import com.influxdb.client.WriteApi;
import com.influxdb.client.write.Point;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.yesaido.ruleengine_server.engine.support.SensorValuePointMapper;
import site.yesaido.ruleengine_server.global.config.InfluxProperties;
import site.yesaido.ruleengine_server.global.dto.SensorValueEvent;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InfluxServiceTest {

    @Mock
    private WriteApi writeApi;

    @Mock
    private InfluxProperties properties;

    @Mock
    private SensorValuePointMapper pointMapper;

    @InjectMocks
    private InfluxService influxService;

    private SensorValueEvent event;
    private Point point;

    @BeforeEach
    void setUp() {
        event = new SensorValueEvent(
                "실습실", "후면 오른쪽",
                "AM107", "AM107-067999", "24e124128c067999",
                "TEMPERATURE", "°C",
                BigDecimal.valueOf(23.7),
                OffsetDateTime.now(),
                10L
        );

        point = mock(Point.class);
    }

    @Test
    void test_save_success() {
        when(properties.getBucket()).thenReturn("sensor-data");
        when(properties.getOrg()).thenReturn("yes-nhn");
        when(pointMapper.toPoint(event)).thenReturn(point);

        influxService.save(event);

        verify(writeApi, times(1)).writePoint("sensor-data", "yes-nhn", point);
    }

    @Test
    void test_save_whenSubmitFails_doesNotThrow() {
        when(pointMapper.toPoint(event)).thenReturn(point);
        doThrow(new RuntimeException("buffer limit exceeded"))
                .when(writeApi).writePoint(any(), any(), any());

        Assertions.assertDoesNotThrow(() -> influxService.save(event));
    }

    @Test
    void test_save_whenPointMapperFails_doesNotThrow() {
        when(pointMapper.toPoint(event)).thenThrow(new IllegalArgumentException("invalid point"));

        Assertions.assertDoesNotThrow(() -> influxService.save(event));

        verify(writeApi, never()).writePoint(any(), any(), any());
    }
}
