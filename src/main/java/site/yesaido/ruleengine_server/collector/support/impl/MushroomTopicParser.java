package site.yesaido.ruleengine_server.collector.support.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import site.yesaido.ruleengine_server.collector.dto.SensorDataDto;
import site.yesaido.ruleengine_server.collector.dto.SupportedTopic;
import site.yesaido.ruleengine_server.collector.support.SensorDataParser;
import site.yesaido.ruleengine_server.global.dto.SensorType;
import site.yesaido.ruleengine_server.global.exception.InvalidPayloadFormatException;
import site.yesaido.ruleengine_server.global.exception.InvalidTopicFormatException;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * {@link site.yesaido.ruleengine_server.collector.support.SensorDataParser}의 <strong>mushroom</strong> 토픽 전용 구현체입니다.<br>
 * 토픽이 <strong>mushroom</strong>으로 시작하는 메세지에 대한 파싱을 담당합니다.
 */
@RequiredArgsConstructor
@Component
public class MushroomTopicParser implements SensorDataParser {

    private final ObjectMapper objectMapper;

    @Override
    public SupportedTopic getSupportedTopic() {
        return SupportedTopic.MUSHROOM;
    }

    @Override
    public boolean supports(String topic) {
        return topic.startsWith(
                this.getSupportedTopic().getPrefix()
        );
    }

    @Override
    public List<SensorDataDto> parse(String topic, String payload) {

        String[] parts = topic.split("/");
        if (parts.length != 6) {
            throw new InvalidTopicFormatException(
                    this.getSupportedTopic(),
                    topic,
                    "토픽 요소 개수가 올바르지 않습니다. (expected: %d, actual: %d)".formatted(6, parts.length)
            );
        }

        String place = parts[1];
        String location = parts[2];
        String deviceModel = parts[3];
        String deviceEui = parts[4];
        SensorType sensorType = SensorType.from(parts[5]);

        MushroomPayload parsed = parsePayload(payload);

        SensorDataDto sensorDataDto = new SensorDataDto(
                place, location,
                deviceModel, parsed.deviceName, deviceEui,
                sensorType,
                parsed.value,
                parsed.time == null ? null : parsed.time.toLocalDateTime()
        );

        return List.of(sensorDataDto);
    }

    // ======================================================================

    private record MushroomPayload(

            Double value,

            OffsetDateTime time,

            @JsonProperty("device_name")
            String deviceName,

            @JsonProperty("device_eui")
            String deviceEui
    ) {}

    private MushroomPayload parsePayload(String payload) {
        try {
            return objectMapper.readValue(payload, MushroomPayload.class);
        } catch (JsonProcessingException e) {
            throw new InvalidPayloadFormatException(
                    this.getSupportedTopic(),
                    "JSON 파싱에 실패했습니다: %s".formatted(e.getOriginalMessage()),
                    payload
            );
        }
    }
}
