package site.yesaido.ruleengine_server.global.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import site.yesaido.common.rabbitmq.DeadLetterQueues;
import site.yesaido.common.rabbitmq.RabbitDeadLetterProperties;

@Configuration
public class RabbitPublisherTopologyConfig {

    @Value("${custom-rabbitmq.queue.notification-threshold}")
    private String notificationThresholdQueueName;

    @Value("${custom-rabbitmq.queue.notification-action}")
    private String notificationActionQueueName;

    @Value("${custom-rabbitmq.exchange.to-notification}")
    private String exchangeToNotification;

    @Bean
    public Queue notificationThresholdQueue(RabbitDeadLetterProperties dlProps) {
        return DeadLetterQueues.durableWithDeadLetter(notificationThresholdQueueName, dlProps).build();
    }

    @Bean
    public Queue notificationActionQueue(RabbitDeadLetterProperties dlProps) {
        return DeadLetterQueues.durableWithDeadLetter(notificationActionQueueName, dlProps).build();
    }

    @Bean
    public DirectExchange notificationExchange() {
        return new DirectExchange(exchangeToNotification, true, false);
    }

    @Bean
    public Binding notificationThresholdBinding(@Qualifier("notificationThresholdQueue") Queue notificationThresholdQueue,
                                                DirectExchange notificationExchange) {
        return BindingBuilder.bind(notificationThresholdQueue)
                .to(notificationExchange)
                .with(notificationThresholdQueueName);
    }

    @Bean
    public Binding notificationActionBinding(@Qualifier("notificationActionQueue") Queue notificationActionQueue,
                                             DirectExchange notificationExchange) {
        return BindingBuilder.bind(notificationActionQueue)
                .to(notificationExchange)
                .with(notificationActionQueueName);
    }
}