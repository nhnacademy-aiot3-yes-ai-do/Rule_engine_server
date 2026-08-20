package site.yesaido.ruleengine_server.registry.component;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import site.yesaido.ruleengine_server.registry.service.ManagedSensorTypeService;

import java.util.List;

/**
 * 애플리케이션 시작 시 기본 관리 대상 센서 타입들을 등록하는 초기화 컴포넌트입니다.
 */
@RequiredArgsConstructor
@Component
public class ManagedSensorTypeInitializer implements ApplicationRunner {

    private final ManagedSensorTypeService managedSensorTypeService;

    /**
     * 시스템 기동 시 기본으로 등록할 센서 타입 목록
     */
    private static final List<String> DEFAULT_SENSOR_TYPES =
            List.of("TEMPERATURE", "HUMIDITY", "CO2", "LIGHT");

    /**
     * 애플리케이션 구동 완료 후 기본 센서 타입들을 관리 목록에 등록합니다.
     *
     * @param args 애플리케이션 구동 인자
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        DEFAULT_SENSOR_TYPES.forEach(managedSensorTypeService::register);
    }
}
