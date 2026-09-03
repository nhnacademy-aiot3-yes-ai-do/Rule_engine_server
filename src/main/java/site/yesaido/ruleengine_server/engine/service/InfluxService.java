package site.yesaido.ruleengine_server.engine.service;

import com.influxdb.client.WriteApi;
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

    private final WriteApi writeApi;
    private final InfluxProperties properties;
    private final SensorValuePointMapper pointMapper;

    public void save(SensorValueEvent event) {
        try {
            writeApi.writePoint(
                    properties.getBucket(),
                    properties.getOrg(),
                    pointMapper.toPoint(event)
            );
        } catch (Exception e) {
            log.error("[InfluxDB] 포인트 적재 준비 실패: ", e);
        }

    }

}