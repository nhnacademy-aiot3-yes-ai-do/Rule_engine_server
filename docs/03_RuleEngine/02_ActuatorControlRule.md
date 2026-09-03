# 02. ActuatorControlRule

센서 평균값이 임계 범위를 벗어난 상태가 일정 시간 지속되면 액추에이터(히터, 가습기 등)를 켜고, 다시 범위 안으로(정확히는 중간값을 넘어) 돌아오면 즉시 끄는 Rule입니다. 과거 문서에서 "제어 명령의 전달 방식은 아직 확정되지 않았습니다"라고 되어 있었지만, 현재는 **`data-generator-server`에 대한 Feign(HTTP) 호출**로 확정되어 구현되어 있습니다.

`supports()`는 `SensorType.contains(dto.getSensorType())`, 즉 `TEMPERATURE`/`HUMIDITY`/`CO2`/`LIGHT` 중 하나일 때만 평가됩니다.

## 1. 액추에이터 종류

```java
public enum ActuatorType {
    HEATER("TEMPERATURE", true),           COOLER("TEMPERATURE", false),
    HUMIDIFIER("HUMIDITY", true),          DEHUMIDIFIER("HUMIDITY", false),
    CO2_SUPPLIER("CO2", true),             VENTILATION_FAN("CO2", false),
    LED("LIGHT", true),                    LIGHT_REDUCER("LIGHT", false);

    private final String targetSensorType;
    private final boolean increasing;   // true: 값을 올리는 방향, false: 낮추는 방향
}
```

센서 타입 하나당 값을 "올리는" 액추에이터와 "낮추는" 액추에이터가 한 쌍으로 존재합니다. (`getOppositeType()`으로 반대쪽을 얻을 수 있습니다)

## 2. evaluate() 전체 흐름

```text
[센서 데이터 수신]
        ↓
[임계값 범위 조회 (sensorType 기준, 단위 무관)] ── 없으면 종료
        ↓
[센서 값을 임계값 범위의 단위로 변환]
        ↓
[변환된 값을 (cultivationId, sensorType, deviceEui) 단위 캐시에 적재] ── 여러 센서의 평균을 내기 위함
        ↓
[(cultivationId, sensorType) 단위 락 획득 시도] ── 실패하면(이미 처리 중) 이번 평가는 건너뜀
        ↓
[현재 액추에이터 상태 조회: None / Pending / Active(type)]
        │
        ├─ Pending(명령 응답 대기 중)이면 → 아무것도 안 하고 종료
        │
        ├─ 평균값으로 목표 상태(target) 계산
        │       - 평균 < 최솟값 → "올리는" 액추에이터가 target
        │       - 평균 > 최댓값 → "낮추는" 액추에이터가 target
        │       - 범위 안이지만 현재 Active 상태고, 아직 중간값을 넘지 않았다면 → 현재 타입 유지 (target = 현재 타입)
        │       - 그 외(범위 안 + 중간값 넘음, 혹은 애초에 None) → target = null
        │
        ├─ None + target == null  → 계속 꺼진 상태 유지
        ├─ None + target != null  → "지속시간 확인 후 ON" 절차 진입
        └─ Active(type) + target == type → 계속 켜진 상태 유지
        └─ Active(type) + target != type → 즉시 OFF 절차 진입
```

## 3. 단위 변환과 평균 계산

```java
BigDecimal convertValue = SensorUnitConverter.convert(sensorData.getValue(), sensorData.getUnit(), range.getUnit());

SensorValueKey key = new SensorValueKey(thresholdInfo.getCultivationId(), sensorData.getSensorType(), sensorData.getDeviceEui());
sensorValueAverageService.put(key, convertValue);

BigDecimal average = sensorValueAverageService.getAverage(thresholdInfo.getCultivationId(), sensorData.getSensorType());
```

- `SensorUnitConverter`는 현재 섭씨(`°C`)/화씨(`°F`) 변환만 지원하며, 그 외 조합은 `IllegalArgumentException`을 던집니다. 단위가 같으면 그대로 반환합니다.
- `SensorValueAverageService`는 `SensorValueKey(cultivationId, sensorType, deviceEui)`를 키로 하는 Caffeine 캐시(`rule-engine.actuator.sensor-value.stale-after-seconds` = 20초 후 만료)에 최신 값을 저장합니다. `getAverage(cultivationId, sensorType)`는 그 재배 환경·센서 타입에 속한 **모든 디바이스의 최근 20초 이내 값**을 평균 냅니다. 즉 한 재배 환경에 같은 종류 센서가 여러 대 있으면, 한 대의 순간적인 튐이 아니라 여러 대의 평균으로 판단합니다.

## 4. 락과 상태

`(cultivationId, sensorType)` 단위로 `ReentrantLock`을 두고(`ConcurrentHashMap<ActuatorControlKey, Lock>`), `tryLock()`(non-blocking)으로 획득을 시도합니다. 실패하면(동시에 다른 스레드가 같은 키를 처리 중) 이번 이벤트에 대한 평가를 그냥 건너뜁니다 — 대기하지 않고, 다음 센서 이벤트가 왔을 때 다시 시도됩니다.

액추에이터 상태(`ActuatorControlStateService`, 현재 구현체는 Caffeine 기반 `CaffeineActuatorControlStateService`, TTL 없음)는 3가지 중 하나입니다.

```java
public sealed interface ActuatorControlState {
    record None() implements ActuatorControlState {}     // 꺼져 있음
    record Pending() implements ActuatorControlState {}   // 명령을 보냈고 응답 대기 중
    record Active(ActuatorType actuatorType) implements ActuatorControlState {}  // 켜져 있음
}
```

`Pending`은 Feign 호출 중 다른 스레드가 같은 키에 대해 중복으로 명령을 보내지 않도록 하는 임시 상태입니다. 명령 전송 함수(`sendCommand`) 호출 직전에 `Pending`으로 바꾸고, 결과에 따라 `None`/`Active`로 되돌립니다.

## 5. 목표 계산 — 중간값(midpoint) 히스테리시스

```java
private ActuatorType determineType(BigDecimal average, SensorRange range, ActuatorControlState currentState) {

    if (average.compareTo(range.getMinValue()) < 0) return ActuatorType.increasingTypeOf(range.getSensorType());
    if (average.compareTo(range.getMaxValue()) > 0) return ActuatorType.decreasingTypeOf(range.getSensorType());

    if (currentState instanceof ActuatorControlState.Active(ActuatorType actuatorType)) {
        BigDecimal midpoint = range.getMinValue().add(range.getMaxValue())
                .divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);

        if (actuatorType.isIncreasing() && average.compareTo(midpoint) < 0) return actuatorType;
        if (!actuatorType.isIncreasing() && average.compareTo(midpoint) > 0) return actuatorType;
    }

    return null;
}
```

값이 최소~최대 범위 "안"으로 들어왔다고 바로 액추에이터를 끄지 않습니다. 예를 들어 히터가 켜져서 값이 최솟값을 살짝 넘어 범위 안으로 들어온 직후라면, 아직 범위의 중간값에도 못 미친 상태이므로 계속 켜둔 채로 중간값을 넘을 때까지 기다립니다. 이 히스테리시스가 없으면 값이 임계값 경계 부근에서 오르내릴 때마다 액추에이터가 껐다 켜졌다를 반복(채터링)하게 됩니다.

## 6. ON — 지속시간(sustain) 확인 후 시작

```java
private void startIfSustained(ActuatorControlKey key, ActuatorType target) {

    TargetSince targetSince = actuatorTargetSinceService.getTargetSince(key);

    if (targetSince == null || targetSince.target() != target) {
        actuatorTargetSinceService.setTargetSince(key, new TargetSince(target, Instant.now()));
        return;   // 목표가 바뀐 최초 시점 — 아직 켜지 않고 타이머만 시작
    }

    Duration elapsed = Duration.between(targetSince.since(), Instant.now());
    if (elapsed.compareTo(sustainedDuration) < 0) {
        return;   // 아직 지속시간(rule-engine.actuator.sustain.minutes = 1분)을 채우지 못함
    }

    actuatorControlStateService.setState(key, new ActuatorControlState.Pending());
    ActuatorCommandResult result = actuatorCommandExecutor.sendCommand(key, target, ActuatorState.ON);

    switch (result) {
        case APPLIED -> {
            actuatorControlStateService.setState(key, new ActuatorControlState.Active(target));
            actuatorTargetSinceService.clear(key);
        }
        case REJECTED_CONFLICT -> actuatorControlStateService.setState(key, new ActuatorControlState.Active(target.getOppositeType()));
        case FAILED -> actuatorControlStateService.setState(key, new ActuatorControlState.None());
    }
}
```

`ActuatorTargetSinceService`(Caffeine 캐시)는 "목표가 이 값으로 정해진 이후 경과 시간"을 `(cultivationId, sensorType)` 단위로 추적합니다. 목표가 바뀔 때마다 타이머가 리셋되므로, 순간적으로 튀는 값 하나 때문에 바로 액추에이터가 켜지지는 않고, 같은 목표가 `sustain.minutes`(기본 1분) 동안 유지되어야 실제로 ON 명령을 보냅니다.

`REJECTED_CONFLICT`(자가 치유): `data-generator-server`가 "반대 액추에이터가 이미 켜져 있다"며 명령을 거절한 경우입니다. 이는 우리 쪽 상태 기록이 실제와 어긋나 있었다는 뜻이므로, 곧바로 재시도하지 않고 상태를 `Active(반대타입)`으로 정정해둔 뒤, 다음 센서 이벤트가 정상 절차(반대 타입이 목표와 다르므로 → 즉시 OFF → 이후 필요시 재 ON)를 밟도록 둡니다.

## 7. OFF — 즉시 정지

```java
private void stopImmediately(ActuatorControlKey key, ActuatorType currentType) {

    actuatorControlStateService.setState(key, new ActuatorControlState.Pending());
    ActuatorCommandResult result = actuatorCommandExecutor.sendCommand(key, currentType, ActuatorState.OFF);

    if (result == ActuatorCommandResult.APPLIED) {
        actuatorControlStateService.setState(key, new ActuatorControlState.None());
    } else {
        actuatorControlStateService.setState(key, new ActuatorControlState.Active(currentType));
    }
}
```

목표가 바뀌어 지금 켜져 있는 것을 꺼야 할 때는 ON과 달리 지속시간을 기다리지 않고 **즉시** OFF 명령을 보냅니다. (OFF 요청은 `data_generator`의 "반대 액추에이터 충돌 검사" 대상이 아니므로 `REJECTED_CONFLICT`가 나올 수 없습니다) 실패하면 상태를 다시 `Active(currentType)`로 되돌려, 실제로는 여전히 켜져 있다고 간주합니다.

## 8. ActuatorCommandExecutor — 실제 Feign 호출

```java
@Service
public class ActuatorCommandExecutor {

    private static final Duration COMMAND_TTL = Duration.ofSeconds(30);

    public ActuatorCommandResult sendCommand(ActuatorControlKey key, ActuatorType actuatorType, ActuatorState desiredState) {

        ActuatorCommandRequest request = new ActuatorCommandRequest(
                UUID.randomUUID(), UUID.randomUUID(), desiredState,
                requestedAt, requestedAt.plus(COMMAND_TTL));

        try {
            ActuatorCommandResponse response =
                    dataGeneratorFeignClient.controlActuator(key.getCultivationId(), actuatorType.name(), request);
            notificationService.sendActuatorCommandResult(key.getCultivationId(), actuatorType.name(), response, requestedAt);
            return ActuatorCommandResult.APPLIED;

        } catch (FeignException e) {
            return handleRejection(key, actuatorType, desiredState, requestedAt, e);   // 응답 바디를 ActuatorCommandResponse로 파싱해 상태 판단
        } catch (Exception e) {
            return ActuatorCommandResult.FAILED;   // 네트워크 오류 등
        }
    }
}
```

```java
@FeignClient(name = "data-generator-server", url = "${feign.client.data-generator-server.url}")
public interface DataGeneratorFeignClient {

    @PutMapping("/api/v1/internal/cultivations/{cultivationId}/actuators/{actuatorType}/state")
    ActuatorCommandResponse controlActuator(@PathVariable Long cultivationId,
                                            @PathVariable String actuatorType,
                                            @RequestBody ActuatorCommandRequest request);
}
```

- 명령에는 `expiresAt`(요청 시각 + 30초)이 실려가며, `data-generator-server`가 만료된 명령은 `REJECTED_EXPIRED`로 거절할 수 있습니다.
- 정상 응답(2xx)이면 `APPLIED`로 간주하고, 성공/실패와 무관하게 결과를 `NotificationService.sendActuatorCommandResult(...)`로 RabbitMQ에 알립니다.
- 4xx 등 예외 응답(`FeignException`)은 본문을 `ActuatorCommandResponse`로 파싱해 `status`를 확인합니다. `REJECTED_CONFLICT`면 `ActuatorCommandResult.REJECTED_CONFLICT`(자가 치유 대상), 그 외(`REJECTED_EXPIRED`, `REJECTED_STALE`)나 파싱 자체가 실패하면 `FAILED`로 처리합니다.
