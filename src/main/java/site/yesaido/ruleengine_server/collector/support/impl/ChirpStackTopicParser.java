package site.yesaido.ruleengine_server.collector.support.impl;

import org.springframework.stereotype.Component;
import site.yesaido.ruleengine_server.collector.dto.SensorDataDto;
import site.yesaido.ruleengine_server.collector.support.SensorDataParser;

import java.util.List;

/**
 * {@link SensorDataParser}의 <strong>ChirpStack</strong> 토픽 전용 구현체입니다.<br>
 * 토픽이 <strong>application</strong>으로 시작하는 메세지에 대한 파싱을 담당합니다.
 */
@Component
public class ChirpStackTopicParser implements SensorDataParser {

    @Override
    public boolean supports(String topic) {
        return topic.startsWith("application/");
    }

    @Override
    public List<SensorDataDto> parse(String topic, String payload) {

        return List.of();
    }
}
