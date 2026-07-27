package site.yesaido.ruleengine_server.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;

@Configuration
public class AsyncConfig {

    // ...

    @Bean(name = "mqttIngestExecutor")
    public Executor mqttIngestExecutor() {

        // todo : mqtt용 Executor 작성

        return null;
    }
}
