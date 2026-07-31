package site.yesaido.ruleengine_server.collector.support.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import site.yesaido.ruleengine_server.collector.dto.SensorDataDto;
import site.yesaido.ruleengine_server.collector.dto.SupportedTopic;
import site.yesaido.ruleengine_server.collector.support.SensorDataParser;
import site.yesaido.ruleengine_server.global.dto.SensorType;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * {@link SensorDataParser}의 <strong>ChirpStack</strong> 토픽 전용 구현체입니다.<br>
 * 토픽이 <strong>application</strong>으로 시작하는 메세지에 대한 파싱을 담당합니다.
 */
@RequiredArgsConstructor
@Component
public class ChirpStackTopicParser implements SensorDataParser {

    private final ObjectMapper objectMapper;

    private static final Map<String, SensorType> OBJECT_KEY_MAPPING = Map.of(
            "temperature", SensorType.TEMPERATURE,
            "humidity", SensorType.HUMIDITY,
            "co2", SensorType.CO2,
            "illumination", SensorType.LIGHT
    );

    @Override
    public SupportedTopic getSupportedTopic() {
        return SupportedTopic.CHIRPSTACK;
    }

    @Override
    public boolean supports(String topic) {
        return topic.startsWith(
                this.getSupportedTopic().getPrefix()
        );
    }

    @Override
    public List<SensorDataDto> parse(String topic, String payload) {

        JsonNode root = readTree(payload);
        JsonNode deviceInfo = root.path("deviceInfo");
        JsonNode tags = deviceInfo.path("tags");
        String place = tags.path("location").asText(null);
        String location = tags.path("point").asText(null);
        String deviceModel = deviceInfo.path("deviceProfileName").asText(null);
        String deviceName = deviceInfo.path("deviceName").asText(null);
        String deviceEui = deviceInfo.path("devEui").asText(null);
        LocalDateTime time = parseTime(root.path("time").asText(null));

        JsonNode object = root.path("object");

        List<SensorDataDto> result = new ArrayList<>();
        object.fields().forEachRemaining(entry -> {
            SensorType sensorType = OBJECT_KEY_MAPPING.get(entry.getKey());
            if (sensorType == null) {
                return;
            }

            result.add(new SensorDataDto(
                    place, location,
                    deviceModel, deviceName, deviceEui,
                    sensorType,
                    entry.getValue().asDouble(), time
            ));
        });

        return result;
    }

    private JsonNode readTree(String payload) {
        try {
            return objectMapper.readTree(payload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private LocalDateTime parseTime(String isoTime) {
        if (isoTime == null) return null;
        return OffsetDateTime.parse(isoTime).toLocalDateTime();
    }
}
