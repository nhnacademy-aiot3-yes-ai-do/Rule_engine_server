package site.yesaido.ruleengine_server.global.config;

import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import site.yesaido.ruleengine_server.engine.dto.ThresholdStatusChangedEvent;
import site.yesaido.ruleengine_server.registry.dto.sensor.SensorInfoUpsertEvent;
import site.yesaido.ruleengine_server.registry.dto.sensor.SensorInfoDeleteEvent;
import site.yesaido.ruleengine_server.registry.dto.threshold.ThresholdInfoEvent;


import java.util.HashMap;
import java.util.Map;

/**
 * RabbitMQ 메시지 직렬화/역직렬화를 위한 JSON 메시지 컨버터 및 클래스 매퍼 설정 클래스입니다.
 */
@Configuration
public class RabbitMqConfig {

    // ...

    @Bean
    public MessageConverter jsonMessageConverter() {
        JacksonJsonMessageConverter converter = new JacksonJsonMessageConverter();

        converter.setClassMapper(classMapper());

        return converter;
    }

    @Bean
    public DefaultClassMapper classMapper() {
        DefaultClassMapper classMapper = new DefaultClassMapper();

        Map<String, Class<?>> idClassMapping = new HashMap<>();
        idClassMapping.put("threshold.crud", ThresholdInfoEvent.class);
        idClassMapping.put("sensor.upsert", SensorInfoUpsertEvent.class);
        idClassMapping.put("sensor.delete", SensorInfoDeleteEvent.class);
        idClassMapping.put("notification.threshold-status", ThresholdStatusChangedEvent.class);
        classMapper.setIdClassMapping(idClassMapping);

        return classMapper;
    }
}
