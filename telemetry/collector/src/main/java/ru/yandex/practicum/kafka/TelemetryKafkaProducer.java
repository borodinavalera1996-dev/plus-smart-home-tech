package ru.yandex.practicum.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelemetryKafkaProducer {

    private final KafkaTemplate<String, SensorEventAvro> sensorKafkaTemplate;
    private final KafkaTemplate<String, HubEventAvro> hubKafkaTemplate;

    private static final String SENSORS_TOPIC = "telemetry.sensors.v1";
    private static final String HUBS_TOPIC = "telemetry.hubs.v1";

    /**
     * Отправка событий датчиков в топик telemetry.sensors.v1
     */
    public void sendSensorEvent(SensorEventAvro event) {
        String key = event.getId();
        log.info("Отправка SensorEventAvro в топик {}: key={}, id={}", SENSORS_TOPIC, key, event.getId());

        sensorKafkaTemplate.send(SENSORS_TOPIC, key, event)
                .whenComplete((result, exception) -> {
                    if (exception != null) {
                        log.error("Ошибка при отправке SensorEventAvro с key={}", key, exception);
                    } else {
                        log.debug("SensorEventAvro успешно доставлен. Partition: {}, Offset: {}",
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }

    /**
     * Отправка событий хабов в топик telemetry.hubs.v1
     */
    public void sendHubEvent(HubEventAvro event) {
        String key = event.getHubId();
        log.info("Отправка HubEventAvro в топик {}: key={}, hubId={}", HUBS_TOPIC, key, event.getHubId());

        hubKafkaTemplate.send(HUBS_TOPIC, key, event)
                .whenComplete((result, exception) -> {
                    if (exception != null) {
                        log.error("Ошибка при отправке HubEventAvro с key={}", key, exception);
                    } else {
                        log.debug("HubEventAvro успешно доставлен. Partition: {}, Offset: {}",
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
}
