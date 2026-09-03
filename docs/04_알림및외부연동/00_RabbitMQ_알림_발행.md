# 00. RabbitMQ 알림 발행

RuleEngine의 판단 결과 중 "알림"에 해당하는 두 가지 — 임계값 초과/복귀, 액추에이터 동작 결과 — 를 RabbitMQ로 발행합니다.

> 과거 문서에는 "실시간 데이터용 큐"가 있었지만, 현재 구현에는 없습니다. 모든 원본 측정값은 RabbitMQ가 아니라 InfluxDB에 저장됩니다. ([01_InfluxDB_저장](./01_InfluxDB_저장.md) 참고) 또한 "제어 명령용 큐"도 RabbitMQ가 아니라 Feign(HTTP) 호출로 대체되었습니다. ([03_RuleEngine/02_ActuatorControlRule](../03_RuleEngine/02_ActuatorControlRule.md) 참고)

## 1. 큐 구성

Direct Exchange(`custom-rabbitmq.exchange.to-notification` = `yes-nhn.notification.exchange`) 하나 아래 큐 2개를 두고, 라우팅 키를 큐 이름과 동일하게 사용합니다.

| 큐 | 라우팅 키 | 발행 이벤트 | 용도 |
|---|---|---|---|
| `notification-threshold` (`yes-nhn.notification.threshold.queue`) | 큐 이름과 동일 | `ThresholdStatusChangedEvent` | 임계값 초과/복귀 알림 |
| `notification-action` (`yes-nhn.notification.action.queue`) | 큐 이름과 동일 | `AutomationStateChangedEvent` | 액추에이터 동작 결과 알림 |

두 큐 모두 durable이며 `x-dead-letter-exchange=yes-nhn.dlx`, `x-dead-letter-routing-key=yes-nhn.dlq`가 설정되어 있습니다. (`RabbitPublisherTopologyConfig`)

## 2. NotificationService

```java
@Service
public class NotificationService {

    private final RabbitTemplate rabbitTemplate;
    private final String exchange;
    private final String thresholdRoutingKey;
    private final String actionRoutingKey;

    @PostConstruct
    void setUpMandatoryReturn() {
        rabbitTemplate.setMandatory(true);
        rabbitTemplate.setReturnsCallback(returned ->
                log.warn("[NotificationService] 라우팅 실패 - 메시지 버려짐: exchange={}, routingKey={}, ...", ...));
    }

    public void sendThresholdExceededAlert(SensorDataDto sensorData) {
        publishThresholdAlert(sensorData, ThresholdStatus.EXCEEDED);
    }

    public void sendThresholdRecoveredAlert(SensorDataDto sensorData) {
        publishThresholdAlert(sensorData, ThresholdStatus.RECOVERED);
    }

    public void sendActuatorCommandResult(long cultivationId, String actuatorType, ActuatorCommandResponse response, Instant requestedAt) {
        AutomationStateChangedEvent event = new AutomationStateChangedEvent(
                response.controlId(), cultivationId, actuatorType,
                response.status().getMessage(),
                response.status() == ActuatorCommandStatus.APPLIED,
                requestedAt.atOffset(ZoneOffset.ofHours(9)));
        rabbitTemplate.convertAndSend(exchange, actionRoutingKey, event);
    }

    private void publishThresholdAlert(SensorDataDto sensorData, ThresholdStatus status) {
        ThresholdStatusChangedEvent event = new ThresholdStatusChangedEvent(
                UUID.randomUUID(), sensorData, status,
                OffsetDateTime.now(ZoneOffset.UTC).withOffsetSameInstant(ZoneOffset.ofHours(9)));
        rabbitTemplate.convertAndSend(exchange, thresholdRoutingKey, event);
    }
}
```

- **`setMandatory(true)` + `setReturnsCallback(...)`**: 발행한 메세지가 어떤 큐로도 라우팅되지 못하면(바인딩이 없거나 잘못된 라우팅 키 등) 브로커가 메세지를 반환(`basic.return`)하는데, 이를 받아서 경고 로그로 남깁니다. 설정하지 않으면 라우팅 실패 메세지는 조용히 버려집니다.
- 시각 필드(`occurredAt`)는 모두 KST(UTC+9)로 맞춰서 발행합니다.

## 3. 발행 이벤트

### ThresholdStatusChangedEvent

```java
public class ThresholdStatusChangedEvent {
    private UUID eventId;
    private SensorDataDto sensorData;     // 임계값을 벗어난(혹은 복귀한) 원본 센서 데이터
    private ThresholdStatus status;       // EXCEEDED | RECOVERED
    private OffsetDateTime occurredAt;
}
```

### AutomationStateChangedEvent

```java
public class AutomationStateChangedEvent {
    private UUID eventId;          // ActuatorCommandResponse.controlId를 그대로 사용
    private long cultivationId;
    private String actuatorType;   // ex) "HEATER"
    private String message;        // ActuatorCommandStatus.getMessage() (사람이 읽을 수 있는 결과 메세지)
    private boolean enabled;       // status == APPLIED 인지 여부
    private OffsetDateTime occurredAt;
}
```

## 4. 메시지 컨버터 클래스 매핑

발행/구독 모두 `RabbitMqConfig`에 등록된 동일한 `idClassMapping`을 사용합니다.

```java
idClassMapping.put("threshold.crud", ThresholdInfoEvent.class);
idClassMapping.put("sensor.upsert", SensorInfoUpsertEvent.class);
idClassMapping.put("sensor.delete", SensorInfoDeleteEvent.class);
idClassMapping.put("notification.threshold-status", ThresholdStatusChangedEvent.class);
idClassMapping.put("notification.action-result", AutomationStateChangedEvent.class);
```

`notification.threshold-status`, `notification.action-result`는 이 서비스가 발행하는 쪽(publisher)이므로, 구독 측(알림 서비스 등)이 메세지 헤더(`__TypeId__`)의 이 문자열 값을 보고 각자의 DTO로 역직렬화하게 됩니다.
