package site.yesaido.ruleengine_server.registry.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import site.yesaido.ruleengine_server.registry.dto.snapshot.CultivationSnapshotResponse;

// data_generator가 쓰는 것과 동일한 cultivation_server 내부 API를 재사용합니다.
@FeignClient(name = "cultivation-server", url = "${feign.client.cultivation-server.url}")
public interface CultivationSnapshotClient {

    @GetMapping("/api/v1/internal/data-generator/snapshot")
    CultivationSnapshotResponse getSnapshot();
}