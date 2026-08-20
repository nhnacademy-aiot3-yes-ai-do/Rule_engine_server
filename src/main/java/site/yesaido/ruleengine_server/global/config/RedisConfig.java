package site.yesaido.ruleengine_server.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 연결 팩토리 및 RedisTemplate 직렬화 방식을 구성하는 설정 클래스입니다.
 */
@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> sessionRedisTemplate = new RedisTemplate<>();
        GenericJacksonJsonRedisSerializer jsonRedisSerializer = GenericJacksonJsonRedisSerializer.builder().build();

        sessionRedisTemplate.setConnectionFactory(redisConnectionFactory);
        sessionRedisTemplate.setKeySerializer(new StringRedisSerializer());
        sessionRedisTemplate.setValueSerializer(jsonRedisSerializer);
        sessionRedisTemplate.setHashKeySerializer(new StringRedisSerializer());
        sessionRedisTemplate.setHashValueSerializer(jsonRedisSerializer);

        return sessionRedisTemplate;
    }
}
