package site.yesaido.ruleengine_server.collector.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CollectorService {

    // todo: CollectorService 구현

    public void ingest(String topic, String payload) {
        log.info("[CollectorService] 메세지 수신 완료: topic={}, payload={}", topic, payload);
    }
}
