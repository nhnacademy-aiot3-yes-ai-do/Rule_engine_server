package site.yesaido.ruleengine_server.collector.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class SensorDataReadyEventListener {

    @Async("taskExecutor")
    @EventListener
    public void handleSensorDataReady(SensorDataReadyEvent event) {
        log.info("이벤트 수신: {}", event.getSensorDataDto());
    }
}
