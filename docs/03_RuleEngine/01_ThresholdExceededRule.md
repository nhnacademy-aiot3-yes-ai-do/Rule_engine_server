# 01. ThresholdExceededRule

센서 값이 임계값을 벗어났는지(초과) / 다시 정상 범위로 돌아왔는지(복귀)를 감지해서 알림을 발행하는 Rule입니다. `supports()`가 항상 `true`이므로, 모든 센서 데이터에 대해 평가됩니다.

```java
@Component
public class ThresholdExceededRule implements Rule {

    private final NotificationService notificationService;
    private final AlertCooldownService alertCooldownService;

    @Override
    public boolean supports(SensorDataDto dto) {
        return true;
    }

    @Override
    public void evaluate(SensorDataDto sensorData, ThresholdInfoDto thresholdInfo) {

        SensorRange sensorRange = thresholdInfo.getRange(sensorData.getSensorType(), sensorData.getUnit());
        if (sensorRange == null) {
            return;
        }

        boolean exceeded = sensorData.getValue().compareTo(sensorRange.getMinValue()) < 0
                || sensorData.getValue().compareTo(sensorRange.getMaxValue()) > 0;

        SensorKey sensorKey = new SensorKey(sensorData.getDeviceEui(), sensorData.getSensorType(), sensorData.getUnit());

        if (exceeded) {
            if (alertCooldownService.tryMarkExceeded(sensorKey)) {
                notificationService.sendThresholdExceededAlert(sensorData);
            }
            return;
        }

        if (alertCooldownService.tryMarkNormal(sensorKey)) {
            notificationService.sendThresholdRecoveredAlert(sensorData);
        }
    }
}
```

## 1. 임계값 조회는 `(sensorType, unit)` 정확히 일치하는 것만

`thresholdInfo.getRange(sensorType, unit)`은 `sensorType_unit`을 키로 저장된 맵에서 정확히 일치하는 항목만 찾습니다. 단위 변환은 하지 않습니다. (뒤에 나올 `ActuatorControlRule`은 `findRangeBySensorType`으로 단위 무관하게 찾은 뒤 직접 단위를 변환한다는 차이가 있습니다)

## 2. AlertCooldownService — 왜 필요한가

임계값을 초과한 센서는, 정상으로 돌아오기 전까지 짧은 주기로 계속 값을 보냅니다. 매번 초과 상태를 알림으로 보내면 알림이 폭주하므로, "초과 시작"과 "정상 복귀" 시점에만 한 번씩 알림을 보내도록 상태와 쿨다운을 관리합니다.

`AlertCooldownService`는 인터페이스이며, 현재 구현체는 Caffeine 기반의 `CaffeineAlertCooldownService` 하나입니다.

```java
@Service
public class CaffeineAlertCooldownService implements AlertCooldownService {

    private final Cache<SensorKey, Boolean> cache;      // 쿨다운 윈도우 (rule-engine.alert.cooldown.minutes = 5분)
    private final Cache<SensorKey, Boolean> stateMap;    // 현재 초과 상태 여부 (rule-engine.alert.state.ttl-hours = 24시간)

    public boolean tryMarkExceeded(SensorKey sensorKey) {
        stateMap.put(sensorKey, true);
        return cache.asMap().putIfAbsent(sensorKey, true) == null;
    }

    public boolean tryMarkNormal(SensorKey sensorKey) {
        Boolean wasExceeded = stateMap.asMap().remove(sensorKey);
        if (Boolean.TRUE.equals(wasExceeded)) {
            cache.invalidate(sensorKey);
            return true;
        }
        return false;
    }
}
```

- `SensorKey`는 `(deviceEui, sensorType, unit)` 조합이라, 같은 센서라도 단위가 다르면 별개로 관리됩니다.
- **`tryMarkExceeded`**: `stateMap`에는 항상 "초과 중"으로 기록하고, `cache`에는 `putIfAbsent`로 최초 1회만 값을 넣습니다. 즉 쿨다운(5분) 내에 반복 호출되면 `cache`에 이미 값이 있어 `putIfAbsent`가 `null`이 아닌 기존 값을 반환하므로 `false`가 되어 **알림을 다시 보내지 않습니다.** 쿨다운이 지나 `cache`에서 만료되면 다음 초과 시 다시 `true`를 반환해 알림을 보냅니다. (즉, 계속 초과 상태가 지속되면 5분마다 한 번씩 재알림)
- **`tryMarkNormal`**: `stateMap`에서 제거한 값이 `true`(직전까지 초과 상태였음)였을 때만 `cache`를 무효화하고 `true`를 반환해 복귀 알림을 보냅니다. 애초에 초과 상태가 아니었다면(정상 값이 계속 들어오는 경우) 매번 `false`를 반환해 복귀 알림이 반복되지 않습니다.
- `stateMap`의 TTL(24시간)은 `cache`의 쿨다운(5분)보다 훨씬 길게 잡아, 오랫동안 값이 안 들어와도 "마지막으로 관측된 상태"가 쉽게 사라지지 않도록 합니다.

## 3. 알림 발행

`NotificationService.sendThresholdExceededAlert` / `sendThresholdRecoveredAlert`가 `ThresholdStatusChangedEvent`를 RabbitMQ로 발행합니다. 발행 상세는 [04_알림및외부연동/00_RabbitMQ_알림_발행](../04_알림및외부연동/00_RabbitMQ_알림_발행.md) 참고.
