package site.yesaido.ruleengine_server.global.initializer;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import site.yesaido.ruleengine_server.registry.service.ManagedSensorTypeService;

import java.util.List;

@RequiredArgsConstructor
@Component
public class ManagedSensorTypeInitializer implements ApplicationRunner {

    private final ManagedSensorTypeService managedSensorTypeService;

    private static final List<String> DEFAULT_SENSOR_TYPES =
            List.of("TEMPERATURE", "HUMIDITY", "CO2", "LIGHT");

    @Override
    public void run(ApplicationArguments args) throws Exception {
        DEFAULT_SENSOR_TYPES.forEach(managedSensorTypeService::register);
    }
}
