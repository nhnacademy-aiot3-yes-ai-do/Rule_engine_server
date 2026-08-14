package site.yesaido.ruleengine_server.global.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.listener.api.RabbitListenerErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 리스너에서 메시지 유효성 검증 실패 시 예외를 처리하는 에러 핸들러 설정 클래스입니다.
 */
@Slf4j
@Configuration
public class RabbitErrorHandlerConfig {

    @Bean
    public RabbitListenerErrorHandler validationErrorHandler() {
        return (amqpMessage, channel, message, exception) -> {
            log.warn("[RabbitMQ] 메세지 처리 실패 - body: {}, cause: {}",
                    new String(amqpMessage.getBody()),
                    exception.getCause() != null ? exception.getCause().getClass().getSimpleName() : exception.getClass().getSimpleName(),
                    exception);
            return null;
        };
    }
}
