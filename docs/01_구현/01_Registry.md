# 01_Registry

Registry에 대한 구현 사항에 대해 서술합니다.

(세부 로직 수정 등 최적화와 관련한 사항에 대해서는 추구에 고려합니다.)

## 0. registry

registry는 크게 consumer/service/repository로 구분되며, 담당하는 기능은 다음과 같습니다.
- **consumer** : `RabbitMQ Listener`를 통한 재배 환경 정보 및 센서 정보 수신
- **service** : `consumer` → `@Service` → `@Repository` 계층 구조를 통한 데이터 전달
- **repository** : 수신한 정보를 `Redis`에 적제

---

## 1. Consumer

`consumer`은 RabbitMQ 큐에 적재된 메세지를 수신하는 역할을 담당합니다.

### 1-1. 구조

재배 환경, 센서 각각에 대해 하나의 큐(`cultivation-info`, `sensor-info`)를 사용하며, 생성/수정 이벤트와 삭제 이벤트를 하나의 큐에서 함께 수신합니다. 이벤트 종류(생성/수정/삭제)에 대한 구분은 `@RabbitHandler`를 통한 페이로드 타입 기반 자동 분기로 처리하며, `consumer` 내부에는 별도의 조건 분기 로직이 존재하지 않습니다.

```
CultivationInfoConsumer
├── consumeCultivationInfoUpsert(CultivationInfoDto)     ← 생성/수정
└── consumeCultivationInfoDelete(CultivationInfoDeleteDto) ← 삭제

SensorInfoConsumer
├── consumeSensorInfoUpsert(SensorInfoDto)     ← 생성/수정
└── consumeSensorInfoDelete(SensorInfoDeleteDto) ← 삭제
```

### 1-2. 타입 기반 자동 분기

메시지가 어떤 이벤트(생성/수정/삭제)에 해당하는지는 메시지 헤더(`__TypeId__`)에 담긴 문자열 값을 기준으로 판별합니다. 이 값과 실제 역직렬화할 DTO 클래스 간의 매핑은 `MessageConverter`(`RabbitMqConfig`)에 등록된 `idClassMapping`을 통해 관리합니다.

```java
idClassMapping.put("cultivation.upsert", CultivationInfoDto.class);
idClassMapping.put("cultivation.delete", CultivationInfoDeleteDto.class);
idClassMapping.put("sensor.upsert", SensorInfoDto.class);
idClassMapping.put("sensor.delete", SensorInfoDeleteDto.class);
```

메시지가 역직렬화되어 특정 DTO 타입의 객체로 만들어지면, 클래스 레벨에 `@RabbitListener`가 선언된 `consumer` 내부의 여러 `@RabbitHandler` 메서드 중 해당 타입을 파라미터로 받는 메서드가 자동으로 호출됩니다. 발행 측은 자신의 클래스명과 무관하게, 위 표에서 정의한 문자열 값(`cultivation.upsert` 등)만 헤더에 실어 발행하면 됩니다.

### 1-3. 검증

수신한 메세지는 `@Payload @Valid`를 통해 각 DTO에 정의된 Bean Validation 제약 조건에 따라 검증됩니다. 검증은 `RabbitValidationConfig`(`RabbitListenerConfigurer`)를 통해 등록된 `Validator`가 리스너 메서드 호출 직전에 수행하며, 검증에 실패한 메세지는 리스너 메서드 내부 코드가 실행되지 않고 곧바로 `errorHandler`로 전달됩니다.

### 1-4. 예외 처리

메세지 파싱(JSON → DTO 변환) 실패, 검증 실패는 모두 `RabbitErrorHandlerConfig`에 정의된 `validationErrorHandler`가 공통으로 처리합니다. 실패한 메세지는 원본 바이트(`amqpMessage.getBody()`)와 함께 로그로 남기고 폐기하며, 별도의 재시도나 DLQ 전송은 현재 구현되어 있지 않습니다.

```java
@RabbitListener(queues = "${rabbitmq.queue.cultivation-info}", errorHandler = "validationErrorHandler")
@Component
public class CultivationInfoConsumer {

    private final CultivationInfoService cultivationInfoService;

    @RabbitHandler
    public void consumeCultivationInfoUpsert(@Payload @Valid CultivationInfoDto thresholdInfoEvent) {
        cultivationInfoService.upsertCultivationInfo(thresholdInfoEvent);
    }

    @RabbitHandler
    public void consumeCultivationInfoDelete(@Payload @Valid CultivationInfoDeleteDto thresholdInfoDeleteEvent) {
        cultivationInfoService.deleteCultivationInfo(thresholdInfoDeleteEvent);
    }
}
```

`SensorInfoConsumer`도 동일한 구조로, `SensorInfoService`에 위임하는 형태로 구현되어 있습니다.

### 1-5. Consumer가 직접 다루지 않는 것

`consumer`는 수신·역직렬화·검증까지만 담당하며, 이후의 비즈니스 로직(재배 환경 존재 여부 확인, Redis 반영 방식 등)은 전부 `service` 계층에 위임합니다. `consumer` 내부에는 `Repository`에 대한 직접 의존이 없습니다.

---

## 2. Service

`service`는 `consumer`로부터 검증이 완료된 DTO를 전달받아, Redis 반영 전에 필요한 부가 로직을 수행한 뒤 `repository`에 위임하는 역할을 담당합니다.

### 2-1. 구조

`CultivationInfoService`, `SensorInfoService`로 도메인 단위 분리되어 있으며, 각각 대응되는 `Repository`를 주입받아 사용합니다. `SensorInfoService`는 센서가 속한 재배 환경의 존재 여부를 확인하기 위해 `CultivationInfoRepository`도 함께 주입받습니다.

```
CultivationInfoService
├── upsertCultivationInfo(CultivationInfoDto)
└── deleteCultivationInfo(CultivationInfoDeleteDto)

SensorInfoService
├── upsertSensorInfo(SensorInfoDto)
└── deleteSensorInfo(SensorInfoDeleteDto)
```

### 2-2. 생성/수정 통합 처리 (Upsert)

생성과 수정 이벤트는 필드 구성이 동일하고, Redis 저장 시에도 "최신 상태로 덮어쓰기"라는 동일한 동작을 수행하므로, 별도로 구분하지 않고 하나의 `upsert` 메서드로 통합하여 처리합니다. 메세지 도착 순서가 보장되지 않는 비동기 환경에서, 생성/수정을 구분해 조건부로 저장하는 방식(`setIfAbsent`/`setIfPresent`)은 오히려 최신 데이터가 유실될 수 있어 채택하지 않았습니다.

### 2-3. 재배 환경 미존재 시 처리 (SensorInfoService)

센서 등록 시점에 해당 센서가 속한 재배 환경이 아직 Redis에 존재하지 않을 수 있습니다. 이는 재배 환경 생성 메세지와 센서 등록 메세지의 도착 순서가 보장되지 않는 데서 발생하는 일시적인 상태이며, 실제로는 정상적인 흐름일 수 있습니다. 따라서 이 경우에도 센서 정보 저장 자체는 막지 않고, 경고 로그만 남긴 뒤 정상적으로 저장을 진행합니다.

```java
if (!cultivationInfoRepository.exists(sensorInfoDto.getCultivationId())) {
    log.warn("센서(deviceModel={}, deviceEui={}, sensorType={})가 추가될 재배 환경(cultivationId={})이 존재하지 않음", ...);
}
sensorInfoRepository.upsertSensorInfo(sensorInfoDto);
```

### 2-4. 삭제 대상 미존재 시 처리

삭제 요청 대상이 Redis에 존재하지 않는 경우(이미 삭제되었거나, 동기화 이전 상태) 역시 삭제 자체를 막지 않습니다. Redis의 `DELETE`는 대상 키가 없어도 오류 없이 정상 처리되므로, 이례적인 상황을 관측할 수 있도록 경고 로그만 남기고 그대로 삭제를 진행합니다.

### 2-5. Service가 직접 다루지 않는 것

`service`는 `RedisTemplate`이나 Redis 키 구조를 직접 알지 못하며, 저장/조회/삭제와 관련된 모든 세부 구현은 `repository`에 위임합니다. 메세지 형식 검증(`@Valid`)은 `consumer` 단계에서 이미 끝난 상태로 전달되므로, `service`는 필드 존재 여부 검증을 다시 수행하지 않습니다.

---

## 3. Repository

`repository`는 Redis에 대한 실제 저장·조회·삭제·존재 확인을 담당하는 계층입니다.

### 3-1. 구조

`Redis` 외의 저장소로 교체될 가능성을 고려하여 인터페이스(`CultivationInfoRepository`, `SensorInfoRepository`)로 정의하고, 각각의 Redis 기반 구현체(`CultivationInfoRedisRepository`, `SensorInfoRedisRepository`)를 별도로 둡니다.

```
CultivationInfoRepository (interface)
└── CultivationInfoRedisRepository
    ├── upsertCultivationInfo(CultivationInfoDto)
    ├── deleteCultivationInfo(Long cultivationId)
    └── exists(Long cultivationId)

SensorInfoRepository (interface)
└── SensorInfoRedisRepository
    ├── upsertSensorInfo(SensorInfoDto)
    ├── deleteSensorInfo(Long cultivationId, SensorType sensorType)
    └── exists(Long cultivationId, SensorType sensorType)
```

### 3-2. Redis Key 설계

| 도메인 | Key 형식 | 예시 |
|---|---|---|
| CultivationInfo | `cultivation:{cultivationId}` | `cultivation:1` |
| SensorInfo | `sensor:{cultivationId}:{sensorType}` | `sensor:1:TEMPERATURE` |

`SensorType`은 Redis Key에 사용될 때 `name()`(enum 상수명)을 사용하며, 사용자 표시용 한글 명칭(`koreanName`)은 사용하지 않습니다. 표시용 값은 변경 가능성이 있어 Key로 사용할 경우 기존 데이터 조회가 깨질 수 있기 때문입니다.

Key 조립 로직은 각 구현체 내부의 `buildKey()` private 메서드로 일원화하여, 저장/조회/삭제/존재확인 메서드 간 키 생성 규칙이 어긋나지 않도록 합니다.

### 3-3. Upsert 동작

`upsert` 메서드는 Redis의 `SET` 커맨드(`opsForValue().set()`)를 그대로 사용합니다. 키의 존재 여부와 무관하게 항상 값을 덮어쓰며, 조건부 저장(`SETNX`, `SET...XX`)은 사용하지 않습니다.

### 3-4. 직렬화 방식

`RedisTemplate`의 Value/HashValue 직렬화는 `GenericJacksonJsonRedisSerializer`(Jackson 3 기반)를 사용합니다. Key/HashKey는 `StringRedisSerializer`를 사용합니다.

### 3-5. 존재 확인 (exists)

Redis의 `EXISTS` 커맨드(`hasKey()`)를 사용하여, 값 전체를 역직렬화하지 않고 키의 존재 여부만 가볍게 확인합니다. `hasKey()`의 반환 타입(`Boolean`)이 `null`일 가능성을 고려하여 `Boolean.TRUE.equals(...)`로 감싸 반환합니다.

```java
@Override
public boolean exists(Long cultivationId) {
    return Boolean.TRUE.equals(redisTemplate.hasKey(buildKey(cultivationId)));
}
```

### 3-6. Repository가 직접 다루지 않는 것

`repository`는 순수하게 Redis 저장소 접근만 담당하며, "재배 환경이 존재하지 않는데 센서를 저장해도 되는가"와 같은 비즈니스 판단은 하지 않습니다. 이러한 판단(로그 기록 여부 등)은 `service` 계층의 책임입니다.
