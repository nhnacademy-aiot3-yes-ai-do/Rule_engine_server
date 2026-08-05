package site.yesaido.ruleengine_server.global.config;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.mqttv5.client.MqttAsyncClient;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class MqttClientConfig {

    @Value("${mqtt.broker-url}")
    private String brokerUrl;

    @Value("${mqtt.client-id}")
    private String clientId;

    @Bean
    public MqttAsyncClient mqttAsyncClient() throws MqttException {
        log.debug("MQTT 접속 정보: brokerUrl={}, clientId={}", brokerUrl, clientId);
        return new MqttAsyncClient(brokerUrl, clientId, new MemoryPersistence());
    }

    @Bean
    public MqttConnectionOptions mqttConnectionOptions() {
        MqttConnectionOptions options = new MqttConnectionOptions();

        options.setAutomaticReconnect(true);
        options.setCleanStart(false);
        options.setConnectionTimeout(10);

        return options;
    }

}
