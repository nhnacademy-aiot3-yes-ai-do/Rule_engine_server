package site.yesaido.ruleengine_server.global.config;

import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import site.yesaido.ruleengine_server.registry.dto.cultivation.CultivationInfoDeleteDto;
import site.yesaido.ruleengine_server.registry.dto.cultivation.CultivationInfoDto;
import site.yesaido.ruleengine_server.registry.dto.sensor.SensorInfoDeleteDto;
import site.yesaido.ruleengine_server.registry.dto.sensor.SensorInfoDto;


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
        idClassMapping.put("cultivation.upsert", CultivationInfoDto.class);
        idClassMapping.put("cultivation.delete", CultivationInfoDeleteDto.class);
        idClassMapping.put("sensor.upsert", SensorInfoDto.class);
        idClassMapping.put("sensor.delete", SensorInfoDeleteDto.class);
        classMapper.setIdClassMapping(idClassMapping);

        return classMapper;
    }
}
