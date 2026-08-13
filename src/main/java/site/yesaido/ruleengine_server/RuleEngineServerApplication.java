package site.yesaido.ruleengine_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import site.yesaido.ruleengine_server.global.config.InfluxProperties;

@EnableConfigurationProperties(value = InfluxProperties.class)
@SpringBootApplication
public class RuleEngineServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(RuleEngineServerApplication.class, args);
    }

}
