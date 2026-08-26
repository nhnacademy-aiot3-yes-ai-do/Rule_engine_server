package site.yesaido.ruleengine_server.global.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitPublisherTopologyConfig {

    @Value("${custom-rabbitmq.queue.notification-threshold}")
    private String notificationThresholdQueueName;

    @Value("${custom-rabbitmq.queue.notification-action}")
    private String notificationActionQueueName;

    @Value("${custom-rabbitmq.exchange.to-notification}")
    private String exchangeToNotification;

    @Bean
    public Queue notificationThresholdQueue() {
        return QueueBuilder.durable(notificationThresholdQueueName)
                .withArgument("x-dead-letter-exchange", "yes-nhn.dlx")
                .withArgument("x-dead-letter-routing-key", "yes-nhn.dlq")
                .build();
    }

    @Bean
    public Queue notificationActionQueue() {
        return QueueBuilder.durable(notificationActionQueueName)
                .withArgument("x-dead-letter-exchange", "yes-nhn.dlx")
                .withArgument("x-dead-letter-routing-key", "yes-nhn.dlq")
                .build();
    }

    @Bean
    public DirectExchange notificationExchange() {
        return new DirectExchange(exchangeToNotification, true, false);
    }

    @Bean
    public Binding notificationThresholdBinding(Queue notificationThresholdQueue, DirectExchange notificationExchange) {
        return BindingBuilder.bind(notificationThresholdQueue)
                .to(notificationExchange)
                .with(notificationThresholdQueueName);
    }

    @Bean
    public Binding notificationActionBinding(Queue notificationActionQueue, DirectExchange notificationExchange) {
        return BindingBuilder.bind(notificationActionQueue)
                .to(notificationExchange)
                .with(notificationActionQueueName);
    }
}