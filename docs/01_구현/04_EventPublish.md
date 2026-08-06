# 04_EventPublish

Collector에서 RuleEngine에게 데이터를 전달하기 위한 이벤트 발헹에 대한 구현 사항에 대해 서술합니다.

## 0. `ApplicationEventPublisher`

`ApplicationEventPublisher`는 **Spring Framework**에서 제공하는 **이벤트 발행 인터페이스**입니다.

`publishEvent(event)` 메서드를 호출하여 이벤트를 발행하면, 해당 이벤트를 감시하도록 등록된 `@EventListenr` 메서드가 호출되어 **파라미터로 이벤트 객체를 전달**받아 처리합니다.

이를 통해 **Collector과 RuleEngine 모듈 간의 직접적인 의존성을 제거**할 수 있습니다.

---

## 1. SensorDataReadyEvent

수신한 MQTT 메세지의 파싱·검증이 완료되어 RuleEngine에게 전달할 데이터가 준비되었음을 알리는 이벤트입니다.
- 발행 시점 : `SensorDataParsr` 및 `SensorDataValidator`를 통과한 정상 데이터가 생성된 직후 발행됩니다.
- 주요 역할 : 모듈 간 직접적인 참조 없이, 검증이 끝난 데이터를 이벤트 형태로 전달합니다.

```java
@AllArgsConstructor
@Getter
public class SensorDataReadyEvent {

    private final SensorDataDto sensorDataDto;
}
```

이벤트 발행 시 후속 로직(RuleEngine)에서 즉시 활용할 수 있도록 데이터를 캡슐화하여 전달합니다.

---

## 2. SensorDataReadyEventListener

발행된 SensorDataReadyEvent를 수신하여, 검증을 통과한 센서 데이터를 전달받아 RuleEngine 모듈로 전달하는 이벤트 핸들러 컴포넌트입니다.

Collector와 RuleEngine 로작간의 실행 흐름을 분리합니다.

비동기 스레드 풀에서 이벤트를 실행함으로써 MQTT 수신 레드가 블로킹 없이 다음 메세지를 빠르게 처리할 수 있도록 보장합니다.

```java
public class SensorDataReadyEventListener {

    @Async("sensorEventTaskExecutor")
    @EventListener
    public void handleSensorDataReady(SensorDataReadyEvent event) {
        
        // ...
        
    }
}
```

`publishEvent()`를 통해 발행된 `SensorDataReadyEvent` 타입의 이벤트를 자동으로 감지하여 메서드를 실행합니다.
