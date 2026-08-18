package site.yesaido.ruleengine_server.engine.service;

import org.springframework.stereotype.Service;
import site.yesaido.ruleengine_server.collector.dto.SensorDataDto;
import site.yesaido.ruleengine_server.registry.dto.threshold.SensorRange;

// todo : NotificationService 구현

@Service
public class NotificationService {

    public void sendThresholdExceededAlert(SensorDataDto sensorData, SensorRange sensorRange) {

    }

    public void sendThresholdRecoveredAlert(SensorDataDto sensorData, SensorRange sensorRange) {

    }

    /*
        지금은 구분되어있지 않은데, sensAlert 구분이 필요함
        public interface NotificationService {
            void sendThresholdExceededAlert(SensorDataDto sensorData, SensorRange range);
            void sendThresholdRecoveredAlert(SensorDataDto sensorData, SensorRange range);
        }

        알림을 보내는 다양한 방식이 있기 때문에, 인터페이스로 두고 방식별로 Slack 웹훅, 이메일, SMS, 푸시 알림, 사내 메신저 등 구현 할 수도 있음
        그러나 저렇게 다양하게 보내는건 내 역할이 아니라 NotificationServer에게 그 역할이 있음
        나는 데이터를 전달만 해주고, 위에서 언급한 다양한 방법으로 알림을 보내는건 NotificationServer의 역할이기 때문에 interface로 두지 않음

        물론 전달 방식을 여러가지로 생각해서 인터페이스로 둘 수도 있지만, 그건 오버엔지니어링이 될 수 있기 때문에 클래스로 사용

        즉 아래와 같이 구현 예정
        @Service
        public class NotificationService {
            public void sendThresholdExceededAlert(SensorDataDto sensorData, SensorRange range) {...}

            public void sendThresholdRecoveredAlert(SensorDataDto sensorData, SensorRange range) {...}
        }
     */

}
