package site.yesaido.ruleengine_server.collector.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import site.yesaido.ruleengine_server.collector.dto.SensorDataDto;
import site.yesaido.ruleengine_server.collector.exception.InvalidPayloadFormatException;
import site.yesaido.ruleengine_server.collector.support.impl.ChirpStackTopicParser;
import site.yesaido.ruleengine_server.global.dto.SensorType;

import java.math.BigDecimal;
import java.util.List;

class ChirpStackTopicParserTest {

    private ChirpStackTopicParser parser;

    @BeforeEach
    void setup() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        parser = new ChirpStackTopicParser(objectMapper);
    }

    @Test
    void test_supports() {
        Assertions.assertTrue(parser.supports("application/#"));
        Assertions.assertFalse(parser.supports("wrong-topic/#"));
    }

    @Test
    void test_parse_success() {
        String payload = """
                {
                  "time": "2026-07-29T00:28:25.611+00:00",
                  "deviceInfo": {
                    "deviceProfileName": "EM320-TH",
                    "deviceName": "EM320-TH-389010",
                    "devEui": "24e124785c389010",
                    "tags": {
                      "location": "사무실",
                      "point": "사무공간 스위치 옆"
                    }
                  },
                  "object": {
                    "humidity": 54.5,
                    "battery": 60.0,
                    "temperature": 25.3,
                    "co2": 1.1,
                    "illumination": 2.2
                  }
                }
                """;
        List<SensorDataDto> dtoList = parser.parse("application/#", payload);
        Assertions.assertEquals(4, dtoList.size());

        SensorDataDto sampleDto = dtoList.getFirst();
        Assertions.assertEquals("사무실", sampleDto.getPlace());
        Assertions.assertEquals("사무공간 스위치 옆", sampleDto.getLocation());
        Assertions.assertEquals("EM320-TH", sampleDto.getDeviceModel());
        Assertions.assertEquals("EM320-TH-389010", sampleDto.getDeviceName());
        Assertions.assertEquals("24e124785c389010", sampleDto.getDeviceEui());

        SensorDataDto tempDto = dtoList.stream()
                .filter(dto -> dto.getSensorType().equals(SensorType.TEMPERATURE.name()))
                .findFirst()
                .orElseThrow();
        Assertions.assertEquals(BigDecimal.valueOf(25.3), tempDto.getValue());

        SensorDataDto humidityDto = dtoList.stream()
                .filter(dto -> dto.getSensorType().equals(SensorType.HUMIDITY.name()))
                .findFirst()
                .orElseThrow();
        Assertions.assertEquals(BigDecimal.valueOf(54.5), humidityDto.getValue());

        SensorDataDto co2Dto = dtoList.stream()
                .filter(dto -> dto.getSensorType().equals(SensorType.CO2.name()))
                .findFirst()
                .orElseThrow();
        Assertions.assertEquals(BigDecimal.valueOf(1.1), co2Dto.getValue());

        SensorDataDto lightDto = dtoList.stream()
                .filter(dto -> dto.getSensorType().equals(SensorType.LIGHT.name()))
                .findFirst()
                .orElseThrow();
        Assertions.assertEquals(BigDecimal.valueOf(2.2), lightDto.getValue());
    }

    @Test
    void test_parse_fail_throwInvalidPayloadFormatException() {
        String topic = "application/#";
        String wrongPayload = """
                {
                  "time": "2026-07-29T00:28:25.611+00:00",
                  "deviceInfo": {
                    "deviceProfileName": "EM320-TH",
                    "deviceName": "EM320-TH-389010",
                    "devEui": "24e124785c389010",
                    "tags": {
                      "location": "사무실",
                      "point": "사무공간 스위치 옆"
                    }
                  },
                  "object": {
                    "humidity": 54.5,
                    "battery": 60.0,
                    "temperature": 25.3
                  }
                """;
        String deviceEuiNullPayload = """
                {
                  "time": "2026-07-29T00:28:25.611+00:00",
                  "deviceInfo": {
                    "deviceProfileName": "EM320-TH",
                    "deviceName": "EM320-TH-389010",
                    "tags": {
                      "location": "사무실",
                      "point": "사무공간 스위치 옆"
                    }
                  },
                  "object": {
                    "humidity": 54.5,
                    "battery": 60.0,
                    "temperature": 25.3
                  }
                }
                """;
        String timeNullPayload = """
                {
                  "deviceInfo": {
                    "deviceProfileName": "EM320-TH",
                    "deviceName": "EM320-TH-389010",
                    "devEui": "24e124785c389010",
                    "tags": {
                      "location": "사무실",
                      "point": "사무공간 스위치 옆"
                    }
                  },
                  "object": {
                    "humidity": 54.5,
                    "battery": 60.0,
                    "temperature": 25.3
                  }
                }
                """;
        String invalidTimeFormatPayload = """
                {
                  "time": "2026-07-29T00:28:25",
                  "deviceInfo": {
                    "deviceProfileName": "EM320-TH",
                    "deviceName": "EM320-TH-389010",
                    "devEui": "24e124785c389010",
                    "tags": {
                      "location": "사무실",
                      "point": "사무공간 스위치 옆"
                    }
                  },
                  "object": {
                    "humidity": 54.5,
                    "battery": 60.0,
                    "temperature": 25.3
                  }
                }
                """;

        Assertions.assertThrows(InvalidPayloadFormatException.class, () -> parser.parse(topic, wrongPayload));
        Assertions.assertThrows(InvalidPayloadFormatException.class, () -> parser.parse(topic, deviceEuiNullPayload));
        Assertions.assertThrows(InvalidPayloadFormatException.class, () -> parser.parse(topic, timeNullPayload));
        Assertions.assertThrows(InvalidPayloadFormatException.class, () -> parser.parse(topic, invalidTimeFormatPayload));

    }
}
