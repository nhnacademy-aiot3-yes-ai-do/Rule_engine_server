package site.yesaido.ruleengine_server.global.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import site.yesaido.common.rabbitmq.DeadLetterQueues;
import site.yesaido.common.rabbitmq.DeadLetterTopologyConfiguration;
import site.yesaido.common.rabbitmq.RabbitDeadLetterProperties;

/**
 * RabbitMQ의 Exchange, Queue 및 Routing Key 바인딩 등 토폴로지를 구성하는 설정 클래스입니다.
 */
@Configuration
@Import(DeadLetterTopologyConfiguration.class)
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
    public Queue thresholdInfoQueue(RabbitDeadLetterProperties dlProps) {
        return DeadLetterQueues.durableWithDeadLetter(thresholdInfoQueueName, dlProps).build();
    }

    @Bean
    public Queue sensorInfoQueue(RabbitDeadLetterProperties dlProps) {
        return DeadLetterQueues.durableWithDeadLetter(sensorInfoQueueName, dlProps).build();
    }

    @Bean
    public Binding cultivationInfoBinding(@Qualifier("thresholdInfoQueue") Queue thresholdInfoQueue,
                                          TopicExchange sensorExchange) {
        return BindingBuilder.bind(thresholdInfoQueue)
                .to(sensorExchange)
                .with("threshold.*");
    }

    @Bean
    public Binding sensorInfoBinding(@Qualifier("sensorInfoQueue") Queue sensorInfoQueue,
                                     TopicExchange sensorExchange) {
        return BindingBuilder.bind(sensorInfoQueue)
                .to(sensorExchange)
                .with("sensor.*");
    }
}