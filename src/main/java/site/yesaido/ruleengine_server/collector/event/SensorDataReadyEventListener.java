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
        log.debug("이벤트 수신: {}", event.getSensorDataDto());
        try {
            // todo : RuleEngine쪽에서 데이터를 전달받는 부분 작성 완료되면 구현
        } catch (Exception e) {
            log.error("RuleEngine 처리 중 예외 발생: sensorDataDto={}", event.getSensorDataDto(), e);
        }
    }
}
