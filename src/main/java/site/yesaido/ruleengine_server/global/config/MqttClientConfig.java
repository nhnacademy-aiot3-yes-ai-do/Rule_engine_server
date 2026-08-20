package site.yesaido.ruleengine_server.global.config;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.mqttv5.client.MqttAsyncClient;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;

/**
 * MQTT 비동기 클라이언트({@link MqttAsyncClient}) 및 연결 옵션({@link MqttConnectionOptions}) 빈을 구성하는 설정 클래스입니다.
 */
@Slf4j
@Configuration
public class MqttClientConfig {

    @Value("${mqtt.broker-url}")
    private String brokerUrl;

    @Value("${mqtt.client-id}")
    private String clientId;

    @Value("${mqtt.username:}")
    private String username;

    @Value("${mqtt.password:}")
    private String password;

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

        if (StringUtils.hasText(username)) {
            options.setUserName(username);
        }
        if (StringUtils.hasText(password)) {
            options.setPassword(password.getBytes(StandardCharsets.UTF_8));
        }

        return options;
    }

}
