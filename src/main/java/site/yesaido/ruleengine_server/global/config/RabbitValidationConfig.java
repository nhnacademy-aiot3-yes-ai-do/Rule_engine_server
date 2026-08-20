package site.yesaido.ruleengine_server.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

/**
 * RabbitListener 수신 메시지에 대한 Validation 설정을 구성하는 클래스입니다.
 */
@RequiredArgsConstructor
@Configuration
public class RabbitValidationConfig implements RabbitListenerConfigurer {

    private final LocalValidatorFactoryBean validator;

    @Override
    public void configureRabbitListeners(RabbitListenerEndpointRegistrar registrar) {
        registrar.setValidator(this.validator);
    }
}
