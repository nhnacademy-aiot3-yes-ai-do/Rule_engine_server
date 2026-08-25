package site.yesaido.ruleengine_server.engine.service;

import com.influxdb.client.InfluxDBClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import site.yesaido.ruleengine_server.engine.support.SensorValuePointMapper;
import site.yesaido.ruleengine_server.global.config.InfluxProperties;
import site.yesaido.ruleengine_server.global.dto.SensorValueEvent;

@Slf4j
@RequiredArgsConstructor
@Service
public class InfluxService {

    private final InfluxDBClient influxDBClient;
    private final InfluxProperties properties;
    private final SensorValuePointMapper pointMapper;

    public void save(SensorValueEvent event) {
        try {
            influxDBClient.getWriteApiBlocking().writePoint(
                    properties.getBucket(),
                    properties.getOrg(),
                    pointMapper.toPoint(event)
            );
        } catch (Exception e) {
            log.error(
                    "[InfluxDB] write failed: cultivationId={}, deviceEui={}, sensorType={}, timestamp={}",
                    event.cultivationId(), event.deviceEui(), event.sensorType(), event.time(), e
            );
        }

    }

}