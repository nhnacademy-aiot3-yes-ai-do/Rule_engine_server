# 03_CollectorService

CollectorService에 대한 구현 사항에 대해 서술합니다.

(세부 로직 수정 등 최적화와 관련한 사항은 추후에 고려합니다.)

## 0. collector

collector가 담당하는 역할은 크게 3가지로 구분됩니다.
1. **parsing** : mqtt 수신으로 전달받은 토픽과 페이로드의 파싱을 통한 `SensorDataDto` 생성
2. **validate** : 파싱으로 생성된 `SensorDataDto`에 대한 검증 (등록된 센서로부터 들어온 값인지, 물리적으로 유효한 값인지)
3. **event-publish** : `ApplicationEventPublisher`를 활용하여, 파싱·검증이 완료된 데이터를 **이벤트 발행 방식**으로 `RuleEngine`에게 전달

collector는 실질적인 파싱·검증 로직을 직접 수행하지 않으며, 각 기능을 순서대로 호출하여 흐름을 조율합니다.

---

## 1. CollectorService

`CollectorService`는 각 핵심 기능이 구현된 컴포넌트들을 주입받아, 각 기능을 순서대로 호출하여 흐름을 조율합니다.

```java
import org.springframework.context.ApplicationEventPublisher;
import site.yesaido.ruleengine_server.collector.dto.SensorDataDto;
import site.yesaido.ruleengine_server.collector.support.SensorDataParser;
import site.yesaido.ruleengine_server.collector.support.SensorDataValidator;

import java.util.List;

public class CollectorService {

    private final List<SensorDataParser> sensorDataParserList;
    private final SensorDataValidator sensorDataValidator;
    private final ApplicationEventPublisher eventPublisher;

    public void ingest(String topic, String payload) {
        
        // ...
        
    }

    private void validateAndPublish(SensorDataDto sensorDataDto) {
        
        // ...
        
    }
}
```

### 1-1. `ingest(topic, payload)`

1. MQTT 구독을 통해 토픽과 페이로드를 수신하여 `CollectorService.ingest(topic, payload)`를 호출합니다.

2. `SensorDataParser.supports(topic)` 메서드를 활용하여,
   해당 토픽을 처리할 수 있는 파서가 존재하는지 여부를 확인하고 존재한다면 그 파서를 반환하고,
   그렇지 않다면 `UnsupportedTopicException`을 던집니다. 

3. 얻어낸 파서를 통해 토픽과 페이로드를 파싱하여 `SensorDataDto`를 얻어냅니다.
   MQTT로부터 수신한 메세지에 여러 센서의 데이터가 포함될 수 있기에, `List<SensorDataDto>`로 얻어냅니다.
   파싱 대상인 토픽이 잘못되었을 경우 `InvalidTopicFormatException`을 던집니다.
   파싱 대상인 페이로드가 잘못되었을 경우 `InvalidPayloadFormatException`을 던집니다. 

4. 파싱이 완료된 `SensorDataDto`를 `validateAndPublish(sensorDataDto)` 메서드에게 넘겨 검증과 이벤트 발행을 진행합니다.

### 1-2. `validateAndPublish(sensorDataDto)`

파싱이 완료된 `SensorDataDto`를 전달받아 검증 및 이벤트 발행을 진행합니다.

1. `SensorDataValidator.isValid(sensorDataDto)`를 호출하여, 해당 dto의 유효성을 검증합니다.
   등록되지 않은 센서 또는 물리적으로 유효하지 않은 것은 폐기합니다.

2. `ApplicationEventPublisher.publishEvent(sensorDataDtoReadyEvent)`를 호출하여 파싱·검증이 완료된 데이터에 대한 이벤트 발행을 진행합니다.

---

## 2. SensorDataParser

`SensorDataParser`는 MQTT로 수신산 토픽과 페이로드를 파싱하여 `SensorDataDto` 목록을 생성하는 역할을 담당하는 인터페이스입니다.

토픽 중류별(`mushroom/#`, `application/#`)로 메세지 구조가 다르기에, 하나의 구현체가 전부 처리하지 않고 토픽별 구현체로 나누어 처리합니다.

```java
public interface SensorDataParser {

   SupportedTopic getSupportedTopic();

   boolean supports(String topic);

   List<SensorDataDto> parse(String topic, String payload);
}
```

`CollectorService`는 등록된 모든 `SensorDataParser` 구현체 중 `supports(topic)`가 `true`를 반환하는 파서를 찾아 파싱을 위임합니다.
지원하는 파서가 없는 경우 `UnsupportedTopicException`을 던집니다.

### 2-1. MushroomTopicParser

`mushroom`으로 시작하는 토픽을 처리합니다. 토픽 문자열 자체에 센서 식별 정보가 담겨 있으며, 페이로드에는 측정값이 담겨 있습니다.

```text
topic :
mushroom/{place}/{location}/{device_name}/{device_eui}/{sensor_type}

payload :
{"value":...,"time":...,"device_name":...,"device_eui":...}
```

- 토픽을 `/` 기준으로 분리하여 `place`, `location`, `deviceModel`, `deviceEui`, `sensorType`을 추출합니다.
- 토픽 요소 개수가 6개가 아니라면 `InvalidTopicFormatException`을 던집니다.
- 페이로드는 `MushroomPayload` record를 역직렬화하며, JSON 파싱 자체가 실패하거나 필수 필드(`value`, `time`)가 누락된 경우 `InvalidPayloadFormatException`을 던집니다.
- 메세지 하나당 센서 타입이 하나이므로, 항상 원소 1개짜리 `List<SensorDataDto>`를 반환합니다.

### 2-2. ChirpStackTopicParser

`application`으로 시작하는 토픽을 처리합니다. ChirpStack에서 발행하는 형식으로, 센서 식별 정보와 측정값이 모두 페이로드 안에 담겨있습니다.

```text
topic :
application/{applicationId}/device/{deviceEui}/event/up

payload :
{
    "time": "...",
    "deviceInfo": {
        "deviceProfileName": "...",
        "deviceName": "...",
        "devEui": "...",
        "tags": {
            "location": "...",
            "point": "..."
        }
    },
    "object": {
        "temperature": ...,
        "humidity": ...,
        "co2": ...,
        "illumination": ...
    }
}
```

payload에는 핵심 정보를 포함한 다양한 정보들이 담겨 있으며, 그 중 필요한 정보들만 추출하여 `SensorDataDto`를 구성합니다.
- `deviceInfo.devEui`에서 센서 식별자를, `deviceInfo.tags.location`과 `device.tags.point`에서 각각 `place`와 `location`을 추출합니다.
- `object` 필드는 하나의 메세지에 여러 센서 타입의 측정값을 동시에 포함할 수 있어, 아래 매핑 테이블에 정의된 키만 걸러내어 각각 별도의 `SensorDataDto`로 변환합니다.

| object 키     | SensorType  |
|--------------|-------------|
| temperature  | TEMPERATURE |
| humidity     | HUMIDITY    |
| co2          | CO2         |
| illumination | LIGHT       |

- 매핑 테이플에 없는 키는 무시됩니다.
- `deviceInfo.devEui`, `time` 필드가 없거나 `time` 필드의 형식이 올바르지 않은 경우 `InvalidPayloadFormatException`을 던집니다.
- JSON 파싱 자체가 실패한 경우에도 `InvalidPayloadFormatException`을 던집니다.

---

## 3. SensorDataValidator

`SensorDataValidator`는 파싱이 완료된 `SensorDataDto`의 유효성을 검증하고, 검증이 톹과한 데이터에 `cultivationId`를 채워 넣는 역할을 담당합니다.

```java
public class SensorDataValidator {

   public class isValid(SensorDataDto sensorDataDto) {
       
       // ...
      
   }
}
```

### 3-1. 검증 절차

`isValid(sensorDataDto)`는 다음 순서로 검증을 수행합니다.

1. `deviceEui`와 `sensorType` 조합으로 `SensorInfoService.findSensorInfo(deviceEui, sensorType)`를 호출하여 등록된 센서인지 확인합니다. 조회 결과가 없으면 미등록 센서로 판단하여 로그를 남기고 폐기합니다.
2. 물리적으로 유효한 범위 내의 갑인지 확인합니다. 범위를 벗어나면 로그를 남기고 폐기합니다.
3. 위 두 단게를 모두 통과하면, 조회된 `SensorInfo`의 `cultivationId`를 `sensorDataDto`에 채워넣습니다.

### 3-2. 물리적 유효 범위

센서 타입별 아래 범위를 벗어나는 갑은 오류로 판단하여 폐기합니다.

| SensorType  | 최솟값   | 최댓값     |
|-------------|-------|---------|
| TEMPERATURE | -20.0 | 60.0    |
| HUMIDITY    | 0.0   | 100.0   |
| CO2         | 0.0   | 10000.0 |
| LIGHT       | 0.0   | 100.0   |

값(`value`)이 `null`인 경우도 유효하지 않은 것을 간주하고 폐기합니다.

### 3-3. 등록 여부와 임계값 조회의 분리

`SensorDataValidator`는 등록 여부 확인과 물리적 범위 검증만 담당하며, 재배 환경의 임계값은 조회하지 않습니다.
임계값 조회 및 판단은 **RuleEngine**의 책임이며, `SensorDataValidator`는 `cultivationId`를 채워 넣는 것까지만 담당하여 **RuleEngine**에게 필요한 정보를 넘겨줍니다.

---

## 4. 이벤트 발행

이벤트 발행에 대해서는 다음 문서에서 다룹니다.

[04_EventPublish](./04_EventPublish.md)