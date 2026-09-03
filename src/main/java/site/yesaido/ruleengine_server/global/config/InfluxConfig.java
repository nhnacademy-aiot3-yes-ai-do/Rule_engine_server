package site.yesaido.ruleengine_server.global.config;

import com.influxdb.client.*;
import com.influxdb.client.write.events.WriteErrorEvent;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import site.yesaido.ruleengine_server.engine.support.SensorValuePointMapper;

/**
 * InfluxDB 클라이언트({@link InfluxDBClient}) 및 데이터 매핑 관련 빈을 구성하는 설정 클래스입니다.
 */
@Slf4j
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
                            .build()
            ));
        }

        InfluxDBClientOptions options = InfluxDBClientOptions.builder()
                .url(required(properties.getUrl(), "influx.url"))
                .authenticateToken(required(properties.getToken(), "influx.token").toCharArray())
                .org(required(properties.getOrg(), "influx.org"))
                .bucket(required(properties.getBucket(), "influx.bucket"))
                .okHttpClient(httpClient)
                .build();

        return InfluxDBClientFactory.create(options);
    }

    @Bean(destroyMethod = "close")
    public WriteApi influxWriteApi(InfluxDBClient influxDBClient) {

        WriteOptions writeOptions = WriteOptions.builder()
                .batchSize(1000)
                .flushInterval(3000)
                .build();

        WriteApi writeApi = influxDBClient.makeWriteApi(writeOptions);
        writeApi.listenEvents(
                WriteErrorEvent.class,
                event -> log.error("[InfluxDB] 배치 write 실패", event.getThrowable())
        );

        return writeApi;
    }


    @Bean
    public SensorValuePointMapper sensorValuePointMapper() {
        return new SensorValuePointMapper();
    }

    private String required(String value, String property) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(property + " must be configured");
        }
        return value;
    }
}