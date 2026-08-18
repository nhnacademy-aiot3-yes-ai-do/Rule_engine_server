package site.yesaido.ruleengine_server.global.config;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitPublisherTopologyConfig {

    @Value("${custom-rabbitmq.exchange.to-notification}")
    private String exchangeToNotification;

    @Bean
    public DirectExchange notificationExchange() {
        return new DirectExchange(exchangeToNotification, true, false);
    }

}
