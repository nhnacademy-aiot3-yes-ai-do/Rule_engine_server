package site.yesaido.ruleengine_server.engine.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import site.yesaido.ruleengine_server.engine.dto.actuator.ActuatorCommandRequest;
import site.yesaido.ruleengine_server.engine.dto.actuator.ActuatorCommandResponse;

@FeignClient(name = "data-generator-server", url = "${feign.client.data-generator-server.url}")
public interface DataGeneratorFeignClient {

    @PutMapping("/api/v1/internal/cultivations/{cultivationId}/actuators/{actuatorType}/state")
    ActuatorCommandResponse controlActuator(@PathVariable Long cultivationId,
                                            @PathVariable String actuatorType,
                                            @RequestBody ActuatorCommandRequest request);
}
