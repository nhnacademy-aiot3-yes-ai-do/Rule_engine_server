package site.yesaido.ruleengine_server.collector.support.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import site.yesaido.ruleengine_server.collector.dto.SensorDataDto;
import site.yesaido.ruleengine_server.collector.dto.SupportedTopic;
import site.yesaido.ruleengine_server.collector.support.SensorDataParser;
import site.yesaido.ruleengine_server.global.dto.SensorType;
import site.yesaido.ruleengine_server.global.exception.InvalidPayloadFormatException;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * {@link SensorDataParser}의 <strong>ChirpStack</strong> 토픽 전용 구현체입니다.<br>
 * 토픽이 <strong>application</strong>으로 시작하는 메세지에 대한 파싱을 담당합니다.
 */
@Component
public class ChirpStackTopicParser implements SensorDataParser {

    private final ObjectMapper objectMapper;

    public ChirpStackTopicParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper.copy()
                .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
    }

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
        if (deviceEui == null) {
            throw new InvalidPayloadFormatException(
                    this.getSupportedTopic(),
                    "필수 필드(deviceInfo.devEui)가 누락되었습니다.",
                    payload
            );
        }
        OffsetDateTime time = parseTime(root.path("time").asText(null));
        if (time == null) {
            throw new InvalidPayloadFormatException(
                    this.getSupportedTopic(),
                    "필수 필드(time)가 누락되었습니다.",
                    payload
            );
        }

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
                    sensorType.name(),
                    entry.getValue().decimalValue(), time,
                    switch (sensorType) {
                        case TEMPERATURE -> "°C";
                        case HUMIDITY -> "%";
                        case CO2 -> "ppm";
                        case LIGHT -> "lux";
                    }
            ));
        });

        return result;
    }

    private JsonNode readTree(String payload) {
        try {
            return objectMapper.readTree(payload);
        } catch (JsonProcessingException e) {
            throw new InvalidPayloadFormatException(
                    this.getSupportedTopic(),
                    "JSON 파싱에 실패했습니다: %s".formatted(e.getOriginalMessage()),
                    payload
            );
        }
    }

    private OffsetDateTime parseTime(String isoTime) {
        if (isoTime == null) return null;

        try {
            return OffsetDateTime.parse(isoTime);
        } catch (DateTimeParseException e) {
            throw new InvalidPayloadFormatException(
                    this.getSupportedTopic(),
                    "time 필드 형식이 올바르지 않습니다. 지원 형식: OffsetDateTime (예시: yyyy-MM-dd'T'HH:mm:ss.SSSXXX 또는 2026-07-10T08:26:17.922+00:00)",
                    isoTime
            );
        }
    }
}
