package site.yesaido.ruleengine_server.global.config;

import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import site.yesaido.ruleengine_server.registry.dto.sensor.SensorInfoEvent;
import site.yesaido.ruleengine_server.registry.dto.sensor.SensorInfoDeleteEvent;
import site.yesaido.ruleengine_server.registry.dto.threshold.ThresholdInfoEvent;


import java.util.HashMap;
import java.util.Map;

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
        idClassMapping.put("sensor.upsert", SensorInfoEvent.class);
        idClassMapping.put("sensor.delete", SensorInfoDeleteEvent.class);
        classMapper.setIdClassMapping(idClassMapping);

        return classMapper;
    }
}
