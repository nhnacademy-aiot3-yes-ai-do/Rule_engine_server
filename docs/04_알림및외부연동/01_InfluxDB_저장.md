# 01. InfluxDB 저장

과거 문서의 "실시간 데이터용 RabbitMQ 큐"는 채택되지 않았고, 대신 **모든 센서 측정값을 InfluxDB에 시계열 데이터로 저장**하는 방식으로 구현되어 있습니다. `RuleEngine.start(dto)`에서 임계값 정보 조회에 성공한 직후, Rule 평가보다 먼저 저장됩니다. (임계값 초과/정상 여부와 무관하게 항상 저장)

## 1. InfluxService

```java
@Service
public class InfluxService {

    private final InfluxDBClient influxDBClient;
    private final InfluxProperties properties;
    private final SensorValuePointMapper pointMapper;

    public void save(SensorValueEvent event) {
        try {
            influxDBClient.getWriteApiBlocking().writePoint(
                    properties.getBucket(), properties.getOrg(), pointMapper.toPoint(event));
        } catch (Exception e) {
            log.error("[InfluxDB] write failed: cultivationId={}, deviceEui={}, sensorType={}, timestamp={}", ...);
        }
    }
}
```

- Blocking Write API를 사용해 동기적으로 저장합니다.
- 저장 실패 시 예외를 삼키고 로그만 남깁니다. 즉 **InfluxDB 저장 실패가 Rule 평가(알림 발행, 액추에이터 제어)를 막지 않습니다.**

## 2. SensorValuePointMapper

`SensorValueEvent`(= `SensorDataDto`를 정규화한 record)를 InfluxDB `Point`로 변환합니다.

```java
public class SensorValuePointMapper {

    public static final String MEASUREMENT = "sensor_value";
    public static final String VALUE_FIELD = "value";

    public Point toPoint(SensorValueEvent event) {
        Point point = Point.measurement(MEASUREMENT)
                .addField(VALUE_FIELD, event.value())
                .time(event.time().toInstant(), WritePrecision.MS);

        addTag(point, "place", event.place());
        addTag(point, "location", event.location());
        addTag(point, "deviceModel", event.deviceModel());
        addTag(point, "deviceName", event.deviceName());
        addTag(point, "deviceEui", event.deviceEui());
        addTag(point, "sensorType", event.sensorType());
        addTag(point, "unit", event.unit());
        addTag(point, "cultivationId", event.cultivationId().toString());

        return point;
    }
}
```

- Measurement: `sensor_value` 하나로 고정, Field는 `value` 하나 (실제 측정값)
- 나머지(`place`, `location`, `deviceModel`, `deviceName`, `deviceEui`, `sensorType`, `unit`, `cultivationId`)는 모두 Tag로 저장하여, 이 값들 기준으로 필터링/그룹핑 조회가 가능하도록 합니다.
- 값이 `null`이거나 공백인 태그는 아예 추가하지 않습니다(`addTag`가 내부에서 체크).
- `value`, `time`, `cultivationId`는 `null`이면 `NullPointerException`, `deviceEui`/`sensorType`/`unit`이 공백이면 `IllegalArgumentException`을 던집니다. (RuleEngine 단계에서는 이미 검증을 통과한 데이터만 들어오므로 정상 흐름에서는 발생하지 않습니다)

## 3. InfluxConfig — 클라이언트 설정

```java
@Configuration
@EnableConfigurationProperties(InfluxProperties.class)
public class InfluxConfig {

    @Bean(destroyMethod = "close")
    public InfluxDBClient influxDBClient(InfluxProperties properties) {

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

        if (properties.getCloudflare().isConfigured()) {
            httpClient.addInterceptor(chain -> chain.proceed(
                    chain.request().newBuilder()
                            .addHeader("CF-Access-Client-Id", properties.getCloudflare().getAccessClientId())
                            .addHeader("CF-Access-Client-Secret", properties.getCloudflare().getAccessClientSecret())
                            .build()));
        }

        return InfluxDBClientFactory.create(InfluxDBClientOptions.builder()
                .url(properties.getUrl())
                .authenticateToken(properties.getToken().toCharArray())
                .org(properties.getOrg())
                .bucket(properties.getBucket())
                .okHttpClient(httpClient)
                .build());
    }
}
```

- `influx.url`/`influx.org`/`influx.bucket`/`influx.token`이 비어있으면 기동 시점에 `IllegalStateException`으로 즉시 실패합니다.
- InfluxDB가 Cloudflare Access 뒤에 있는 경우를 대비해, `influx.cloudflare.access-client-id`/`access-client-secret`이 설정되어 있으면 모든 요청에 `CF-Access-Client-Id`/`CF-Access-Client-Secret` 헤더를 자동으로 추가하는 OkHttp 인터셉터를 등록합니다. 설정되어 있지 않으면 헤더 없이 그대로 호출합니다.
