package ru.yandex.practicum.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelemetryKafkaProducer {

    private final Producer<String, SensorEventAvro> sensorProducer;
    private final Producer<String, HubEventAvro> hubProducer;

    @Value("${spring.kafka.topics.sensors}")
    private String sensorsTopic;
    @Value("${spring.kafka.topics.hubs}")
    private String hubsTopic;

    /**
     * Отправка событий датчиков в топик
     */
    public void sendSensorEvent(SensorEventAvro event) {
        String key = event.getId();
        log.info("Отправка SensorEventAvro в топик {}: key={}, id={}", sensorsTopic, key, event.getId());

        ProducerRecord<String, SensorEventAvro> record = new ProducerRecord<>(sensorsTopic, key, event);

        sensorProducer.send(record, (metadata, exception) -> {
            if (exception != null) {
                log.error("Ошибка при отправке SensorEventAvro с key={}", key, exception);
            } else {
                log.debug("SensorEventAvro успешно доставлен. Partition: {}, Offset: {}",
                        metadata.partition(),
                        metadata.offset());
            }
        });
    }

    /**
     * Отправка событий хабов в топик
     */
    public void sendHubEvent(HubEventAvro event) {
        String key = event.getHubId();
        log.info("Отправка HubEventAvro в топик {}: key={}, hubId={}", hubsTopic, key, event.getHubId());

        ProducerRecord<String, HubEventAvro> record = new ProducerRecord<>(hubsTopic, key, event);

        hubProducer.send(record, (metadata, exception) -> {
            if (exception != null) {
                log.error("Ошибка при отправке HubEventAvro с key={}", key, exception);
            } else {
                log.debug("HubEventAvro успешно доставлен. Partition: {}, Offset: {}",
                        metadata.partition(),
                        metadata.offset());
            }
        });
    }
}

