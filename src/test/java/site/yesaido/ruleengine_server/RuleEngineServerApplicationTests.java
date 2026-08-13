package site.yesaido.ruleengine_server;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

/**
 * Spring Boot 애플리케이션 컨텍스트 로딩 및 주요 Bean 등록 상태를 검증하는 테스트 클래스입니다.
 */
@SpringBootTest
class RuleEngineServerApplicationTests {

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * Spring ApplicationContext가 정상적으로 초기화되고 메인 애플리케이션 Bean이 등록되었는지 검증합니다.
     */
    @Test
    @DisplayName("Spring ApplicationContext 및 메인 애플리케이션 Bean 정상 로딩 검증")
    void contextLoads() {
        Assertions.assertNotNull(applicationContext, "ApplicationContext가 정상적으로 로딩되어야 합니다.");

        RuleEngineServerApplication mainApp = applicationContext.getBean(RuleEngineServerApplication.class);
        Assertions.assertNotNull(mainApp, "RuleEngineServerApplication Bean이 ApplicationContext에 정상 등록되어 있어야 합니다.");
    }
}

