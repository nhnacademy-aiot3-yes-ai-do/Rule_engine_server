# 00. MQTT 수신

MQTT 브로커와 연결하고, 토픽을 구독하여 메세지를 수신하는 부분입니다. 3개 클래스로 구성되며, 브로커 연결 설정(`MqttClientConfig`)만 `global.config` 패키지에 있고 나머지 둘은 `collector.component` 패키지에 있습니다.

1. **`MqttClientConfig`** (`global.config`) : 브로커 접속 정보와 연결 옵션을 설정하여 `MqttAsyncClient` Bean을 생성합니다.
2. **`MqttConnectionManager`** (`collector.component`) : 애플리케이션 라이프사이클에 맞춰 MQTT 브로커 연결 및 토픽 구독을 수행합니다.
3. **`MqttMessageSubscriber`** (`collector.component`) : 수신된 메세지를 비동기 스레드 풀로 넘겨 `CollectorService`를 호출합니다.

---

## 1. MqttClientConfig

Paho MQTT v5 클라이언트 라이브러리를 기반으로 `MqttAsyncClient`, `MqttConnectionOptions` Bean을 생성합니다.

```java
@Configuration
public class MqttClientConfig {

    @Value("${mqtt.broker-url}")
    private String brokerUrl;

    @Value("${mqtt.client-id}")
    private String clientId;

    @Value("${mqtt.username:}")
    private String username;

    @Value("${mqtt.password:}")
    private String password;

    @Bean
    public MqttAsyncClient mqttAsyncClient() throws MqttException {
        return new MqttAsyncClient(brokerUrl, clientId, new MemoryPersistence());
    }

    @Bean
    public MqttConnectionOptions mqttConnectionOptions() {
        MqttConnectionOptions options = new MqttConnectionOptions();
        options.setAutomaticReconnect(true);
        options.setCleanStart(false);
        options.setConnectionTimeout(10);

        if (StringUtils.hasText(username)) {
            options.setUserName(username);
        }
        if (StringUtils.hasText(password)) {
            options.setPassword(password.getBytes(StandardCharsets.UTF_8));
        }
        return options;
    }
}
```

- `mqtt.username`/`mqtt.password`가 비어있으면 옵션에 아예 설정하지 않습니다. (인증 없는 브로커 접속 허용)
- `setAutomaticReconnect(true)`로 연결이 끊겨도 Paho 클라이언트가 자체적으로 재연결을 시도합니다.
- `setCleanStart(false)`로 재연결 시 이전 세션(구독 정보 등)을 유지하려고 시도합니다.

---

## 2. MqttConnectionManager

Spring의 `SmartLifecycle`을 구현하여, 컨테이너 구동 시점에 브로커 연결과 토픽 구독을 수행하고, 종료 시점에 연결을 해제합니다.

```java
@Component
public class MqttConnectionManager implements SmartLifecycle {

    private final MqttAsyncClient mqttAsyncClient;
    private final MqttConnectionOptions mqttConnectionOptions;
    private final MqttMessageSubscriber mqttMessageSubscriber;

    @Value("${mqtt.subscribe-topics}")
    private String[] topics;

    @Value("${mqtt.enabled}")
    private boolean mqttEnabled;

    private volatile boolean running = false;

    @Override
    public boolean isAutoStartup() {
        return mqttEnabled;
    }

    @Override
    public void start() {
        mqttAsyncClient.setCallback(mqttMessageSubscriber);
        mqttAsyncClient.connect(mqttConnectionOptions).waitForCompletion();

        MqttSubscription[] mqttSubscriptions = Arrays.stream(topics)
                .map(String::trim)
                .map(topic -> new MqttSubscription(topic, 0))
                .toArray(MqttSubscription[]::new);
        mqttAsyncClient.subscribe(mqttSubscriptions).waitForCompletion();

        running = true;
    }

    @Override
    public void stop() {
        mqttAsyncClient.disconnect().waitForCompletion();
        running = false;
    }

    @Override
    public boolean isRunning() {
        return running;
    }
}
```

### 2-1. `mqtt.enabled` 스위치

`isAutoStartup()`이 `mqtt.enabled` 값을 그대로 반환합니다. `mqtt.enabled=false`로 설정하면 `SmartLifecycle`의 자동 시작 대상에서 제외되어, 애플리케이션이 기동되어도 MQTT 연결·구독을 아예 시도하지 않습니다. (MQTT 브로커 없이 다른 부분만 확인하고 싶을 때 사용)

### 2-2. `start()`

1. `mqttMessageSubscriber`를 콜백으로 등록합니다.
2. `mqttConnectionOptions`로 브로커에 연결합니다.
3. `mqtt.subscribe-topics`(콤마로 구분된 문자열, 예: `"mushroom/#, application/#"`)를 공백 제거 후 `MqttSubscription` 배열로 변환하여 QoS 0으로 구독합니다.
4. 연결·구독이 모두 끝나면 `running = true`.
5. 연결 실패 시 `IllegalStateException`을 던져 애플리케이션 시작 자체를 실패시킵니다.

### 2-3. `stop()`

애플리케이션 종료 시 `disconnect()`로 연결을 안전하게 해제합니다.

---

## 3. MqttMessageSubscriber

`MqttCallback` 구현체로, 구독 중인 토픽에 메세지가 도착했을 때 처리를 담당합니다.

```java
@Component
public class MqttMessageSubscriber implements MqttCallback {

    private final CollectorService collectorService;
    private final Executor mqttIngestExecutor;

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) {
        String payload = new String(mqttMessage.getPayload(), StandardCharsets.UTF_8);

        mqttIngestExecutor.execute(() -> {
            try {
                collectorService.ingest(topic, payload);
            } catch (Exception e) {
                log.warn("MQTT 메세지 처리 실패 : topic={}, payload={}", topic, payload, e);
            }
        });
    }

    @Override
    public void disconnected(MqttDisconnectResponse response) { /* 경고 로그만 남김 */ }

    @Override
    public void mqttErrorOccurred(MqttException e) { /* 에러 로그만 남김 */ }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) { /* 재연결 시 로그만 남김 */ }

    @Override
    public void deliveryComplete(IMqttToken token) { /* 구독만 하므로 사용하지 않음 */ }

    @Override
    public void authPacketArrived(int reasonCode, MqttProperties properties) { /* 확장 인증 미사용 */ }
}
```

- 메인 MQTT 콜백 스레드가 블로킹되지 않도록, 실제 처리(`collectorService.ingest(...)`)는 `mqttIngestExecutor` 스레드 풀(`AsyncConfig`에 정의, corePoolSize 5 / maxPoolSize 10 / queueCapacity 100)에 위임합니다.
- 처리 중 예외가 발생해도 캐치하여 로그만 남기고, 콜백 스레드나 스레드 풀이 죽지 않도록 합니다.
