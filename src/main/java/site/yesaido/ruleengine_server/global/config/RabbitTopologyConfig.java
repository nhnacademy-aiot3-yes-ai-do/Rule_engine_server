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

    private static final String MUSH_EXCHANGE = "mushmush-exchange";

    @Value("${custom-rabbitmq.queue.cultivation-info}")
    private String cultivationInfoQueueName;

    @Value("${custom-rabbitmq.queue.sensor-info}")
    private String sensorInfoQueueName;

    @Bean
    public TopicExchange mushmushExchange() {
        return new TopicExchange(MUSH_EXCHANGE, true, false);
    }

    @Bean
    public Queue cultivationInfoQueue() {
        return QueueBuilder.durable(cultivationInfoQueueName).build();
    }

    @Bean
    public Queue sensorInfoQueue() {
        return QueueBuilder.durable(sensorInfoQueueName).build();
    }

    @Bean
    public Binding cultivationInfoBinding(Queue cultivationInfoQueue, TopicExchange mushmushExchange) {
        return BindingBuilder.bind(cultivationInfoQueue)
                .to(mushmushExchange)
                .with("cultivation.*");
    }

    @Bean
    public Binding sensorInfoBinding(Queue sensorInfoQueue, TopicExchange mushmushExchange) {
        return BindingBuilder.bind(sensorInfoQueue)
                .to(mushmushExchange)
                .with("sensor.*");
    }
}