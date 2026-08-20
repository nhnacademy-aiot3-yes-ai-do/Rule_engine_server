package site.yesaido.ruleengine_server.registry.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.yesaido.ruleengine_server.registry.repository.ManagedSensorTypeRepository;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ManagedSensorTypeServiceTest {

    @Mock
    private ManagedSensorTypeRepository managedSensorTypeRepository;

    @InjectMocks
    private ManagedSensorTypeService managedSensorTypeService;

    String sensorType;

    @BeforeEach
    void setup() {
        sensorType = "TEMPERATURE";
    }

    @Test
    @DisplayName("관리 대상 센서 타입 등록")
    void test_register() {

        managedSensorTypeService.register(sensorType);

        verify(managedSensorTypeRepository, times(1)).register(anyString());
        Assertions.assertTrue(managedSensorTypeService.isManaged(sensorType));
    }

    @Test
    @DisplayName("관리 대상 센서 타입 다수 등록")
    void test_registerAll() {

        List<String> sensorTypes = List.of("A", "B", "C");

        managedSensorTypeService.registerAll(sensorTypes);

        verify(managedSensorTypeRepository, times(3)).register(anyString());

        Assertions.assertAll(
                () -> Assertions.assertTrue(managedSensorTypeService.isManaged("A")),
                () -> Assertions.assertTrue(managedSensorTypeService.isManaged("B")),
                () -> Assertions.assertTrue(managedSensorTypeService.isManaged("C"))
        );

    }

    @Test
    void test_isManaged_returnFalse_whenNotRegistered() {

        Assertions.assertFalse(managedSensorTypeService.isManaged("NOT_REGISTERED"));
    }

    @Test
    void test_findAll() {

        Assertions.assertEquals(0, managedSensorTypeService.findAll().size());

        managedSensorTypeService.registerAll(List.of("A", "B"));

        Assertions.assertEquals(2, managedSensorTypeService.findAll().size());
        Assertions.assertTrue(managedSensorTypeService.isManaged("A"));
        Assertions.assertTrue(managedSensorTypeService.isManaged("B"));
    }
}