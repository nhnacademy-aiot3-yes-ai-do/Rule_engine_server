package site.yesaido.ruleengine_server.collector.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import site.yesaido.ruleengine_server.collector.dto.SensorDataDto;
import site.yesaido.ruleengine_server.collector.exception.InvalidPayloadFormatException;
import site.yesaido.ruleengine_server.collector.exception.InvalidTopicFormatException;
import site.yesaido.ruleengine_server.collector.support.impl.MushroomTopicParser;
import site.yesaido.ruleengine_server.global.dto.SensorType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

class MushroomTopicParserTest {

    private MushroomTopicParser parser;

    private final String topic = "mushroom/장소/위치/device_model/device_eui/CO2";

    @BeforeEach
    void setup() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        parser = new MushroomTopicParser(objectMapper);
    }

    @Test
    void test_supports() {
        Assertions.assertTrue(parser.supports(topic));
        Assertions.assertFalse(parser.supports("wrong-topic/{place}/{location}"));
    }

    @Test
    void test_parse_success() {
        String payload = "{\"value\":995.9,\"time\":\"2026-07-10T08:26:17.922+00:00\",\"unit\":\"ppm\",\"device_name\":\"AM107-067999\",\"device_eui\":\"24e124128c067999\"}";
        SensorDataDto dto = parser.parse(topic, payload).getFirst();

        Assertions.assertEquals("장소", dto.getPlace());
        Assertions.assertEquals("위치", dto.getLocation());
        Assertions.assertEquals("device_model", dto.getDeviceModel());
        Assertions.assertEquals("AM107-067999", dto.getDeviceName());
        Assertions.assertEquals("device_eui", dto.getDeviceEui());
        Assertions.assertEquals(SensorType.CO2.name(), dto.getSensorType());
        Assertions.assertEquals(OffsetDateTime.parse("2026-07-10T08:26:17.922+00:00"), dto.getTime());
        Assertions.assertEquals(BigDecimal.valueOf(995.9), dto.getValue());
    }

    @Test
    void test_parse_fail_throwInvalidTopicFormatException() {
        Assertions.assertThrows(InvalidTopicFormatException.class,
                () -> parser.parse("mushroom/#", "{\"value\":995.9}"));
    }

    @Test
    void test_parse_fail_throwInvalidPayloadFormatException() {
        Assertions.assertAll(
                // 올바른 필드명: "value", 현재 필드명: "vale"
                () -> Assertions.assertThrows(InvalidPayloadFormatException.class,
                        () -> parser.parse(topic, "{\"vale\":995.9,\"time\":\"2026-07-10T08:26:17.922+00:00\",\"unit\":\"ppm\",\"device_name\":\"AM107-067999\",\"device_eui\":\"24e124128c067999\"}")),
                // 필수 필드(unit)가 없음
                () -> Assertions.assertThrows(InvalidPayloadFormatException.class,
                        () -> parser.parse(topic, "{\"value\":995.9,\"time\":\"2026-07-10T08:26:17.922+00:00\",\"device_name\":\"AM107-067999\",\"device_eui\":\"24e124128c067999\"}")),
                // 페이로드 끝에 "}"가 빠짐
                () -> Assertions.assertThrows(InvalidPayloadFormatException.class,
                        () -> parser.parse(topic, "{\"value\":995.9,\"time\":\"2026-07-10T08:26:17.922+00:00\",\"unit\":\"ppm\",\"device_name\":\"AM107-067999\",\"device_eui\":\"24e124128c067999\""))
        );
    }
}
