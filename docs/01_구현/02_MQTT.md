# 02_MQTT

MQTT 브로커와 연결, 토픽 구독을 통해 메세지를 수신하는 과정의 구현에 대해 서술합니다.

## 0. Mqtt

위 프로젝트에서 Mqtt에 대한 사항은 크게 3가지로 구분됩니다.

1. **config** : MQTT 브로커 접속 정보와 연결 옵션을 설정하여 `MqttAsyncClient` Bean을 생성합니다.
2. **connection-manager** : 애플리케이션 라이프사이클에 맞춰 MQTT 브로커 연결 및 토픽 구독을 수행합니다.
3. **subscriber** : 수신된 메세지를 비동기 스레드 풀로 전달하여 `CollectorService`를 호출합니다.

---

## 1. MqttClientConfig

`MqttClientConfig`는 Paho MQTT v5 클라이언트 라이브러리를 기반으로 브로커 접속 정보 및 연결 옵션을 설정하는 클래스입니다.

MQTT 브로커 접속 정보와 연결 옵션 설정을 통해 `MqttAsyncClient`와 `MqttConnectionOptions` Bean을 생성합니다.

```java
@Configuration
public class MqttClientConfig {

    @Value("${mqtt.broker-url}")
    private String brokerUrl;

    @Value("${mqtt.client-id}")
    private String clientId;

    @Bean
    public MqttAsyncClient mqttAsyncClient() throws MqttException {
        log.debug("MQTT 접속 정보: brokerUrl={}, clientId={}", brokerUrl, clientId);
        return new MqttAsyncClient(brokerUrl, clientId, new MemoryPersistence());
    }

    @Bean
    public MqttConnectionOptions mqttConnectionOptions() {
        MqttConnectionOptions options = new MqttConnectionOptions();

        options.setAutomaticReconnect(true);
        options.setCleanStart(false);
        options.setConnectionTimeout(10);

        return options;
    }

}
```

- `MqttAsyncClient` : 비동기 방식의 MQTT 클라이언트 객체를 생성합니다.
- `MqttConnectionOptions` : MQTT 브로커 연결 시 적용할 세부 옵션을 설정합니다.

---

## 2. MqttClientManager

`MqttClientManager`는 Spring의 `SmartLifecycle`을 구현하여,
애플리케이션 구동 시점에 MQTT 브로커를 연결하고 지정된 토픽을 구독하는 컴포넌트입니다.

```java
@Component
public class MqttConnectionManager implements SmartLifecycle {

    private final MqttAsyncClient mqttAsyncClient;
    private final MqttConnectionOptions mqttConnectionOptions;
    private final MqttMessageSubscriber mqttMessageSubscriber;

    @Value("${mqtt.subscribe-topics}")
    private String[] topics;

    private volatile boolean running = false;

    @Override
    public void start() {
        
        // ...
        
    }

    @Override
    public void stop() {
        
        // ...
        
    }

    @Override
    public boolean isRunning() {
        return running;
    }
}
```

### 2-1. `start()`

1. `mqttAsyncClient.setCallback(mqttMessageSubscriber)`를 호출하여 메세지 수신 및 연결 상태 이벤트를 처리할 콜백을 지정합니다.
2. `mqttAsyncClient.connect(mqttConnectionOptions).waitForCompletion()`을 통해 브로커와 연결을 맺습니다.
3. `application.yml`에 지정된 `mqtt.subscribe-topics` 배열을 읽어와 `MqttSubscription` 객체 배열로 변환합니다. (QoS 0 적용)
4. `mqttAsyncClient.subscribe(mqttSubscriptions).waitForCompletion()`을 호출하여 토픽 구독을 완료합니다.
5. 연결 및 구독이 정상적으로 완료되면 `running = true`로 상태를 변경합니다.

### 2-2. `stop()`

애플리케이션 종료 시 `mqttAsyncClient.disconnect().waitForCompletion()`을 호출하여 MQTT 연결을 안전하게 해제하고 `running = false`로 변경합니다.

---

## 3. MqttMessageSubscriber

`MqttMessageSubscriber`는 `MqttCallback` 인터페이스의 구현체로, 구독 중인 토픽으로부터 메세지가 도착했을 때 비동기로 처리하는 역할을 담당합니다.

```java
@Component
public class MqttMessageSubscriber implements MqttCallback {

    private final CollectorService collectorService;
    private final Executor mqttIngestExecutor;

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
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
    public void disconnected(MqttDisconnectResponse mqttDisconnectResponse) { ... }

    @Override
    public void mqttErrorOccurred(MqttException e) { ... }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) { ... }
}

```

### 3-1. `messageArrived(topic, mqttMessage)`

1. 수신된 `MqttMessage`의 byte 배열 페이로드를 UTF-8 인코딩 문자열로 변환합니다.
2. 메인 수신 콜백 스레드의 블로킹을 방지하기 위해 `mqttIngestExecutor` 스레드 풀에게 작업을 위임합니다.
3. 스레드 풀 내부에서 `collectorService.ingest(topic, payload)`를 호출하여 파싱·검증 및 이벤트 발행 흐름을 진행합니다.
4. 처리 중 예외가 발생하더라도 수신 스레드가 멈추지 않도록 예외를 캐치하여 로그를 남깁니다.
