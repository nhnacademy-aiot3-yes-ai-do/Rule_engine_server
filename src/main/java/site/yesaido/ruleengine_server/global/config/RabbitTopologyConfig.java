package site.yesaido.ruleengine_server.global.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitTopologyConfig {

    @Value("${custom-rabbitmq.exchange.from-cultivation}")
    private String exchangeFromCultivation;

    @Value("${custom-rabbitmq.queue.threshold-info}")
    private String thresholdInfoQueueName;

    @Value("${custom-rabbitmq.queue.sensor-info}")
    private String sensorInfoQueueName;

    @Bean
    public TopicExchange sensorExchange() {
        return new TopicExchange(exchangeFromCultivation, true, false);
    }

    @Bean
    public Queue thresholdInfoQueue() {
        return QueueBuilder.durable(thresholdInfoQueueName).build();
    }

    @Bean
    public Queue sensorInfoQueue() {
        return QueueBuilder.durable(sensorInfoQueueName).build();
    }

    @Bean
    public Binding cultivationInfoBinding(Queue thresholdInfoQueue, TopicExchange sensorExchange) {
        return BindingBuilder.bind(thresholdInfoQueue)
                .to(sensorExchange)
                .with("threshold.*");
    }

    @Bean
    public Binding sensorInfoBinding(Queue sensorInfoQueue, TopicExchange sensorExchange) {
        return BindingBuilder.bind(sensorInfoQueue)
                .to(sensorExchange)
                .with("sensor.*");
    }
}