package site.yesaido.ruleengine_server.global.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ의 Exchange, Queue 및 Routing Key 바인딩 등 토폴로지를 구성하는 설정 클래스입니다.
 */
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
        return QueueBuilder.durable(thresholdInfoQueueName)
                .withArgument("x-dead-letter-exchange", "yes-nhn.dlx")
                .build();
    }

    @Bean
    public Queue sensorInfoQueue() {
        return QueueBuilder.durable(sensorInfoQueueName)
                .withArgument("x-dead-letter-exchange", "yes-nhn.dlx")
                .build();
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