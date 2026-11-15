package ru.yandex.practicum;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.service.SnapshotService;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * Класс AggregationStarter, ответственный за запуск агрегации данных.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AggregationStarter {

    private final Consumer<String, SensorEventAvro> consumer;
    private final Producer<String, SensorsSnapshotAvro> producer;
    private final SnapshotService snapshotService;

    @Value("${kafka.topics.sensors-events:telemetry.sensors.v1}")
    private String sensorsTopic;

    @Value("${kafka.topics.snapshots:telemetry.snapshots.v1}")
    private String snapshotsTopic;

    @Value("${kafka.consumer.poll-timeout-ms:100}")
    private long pollTimeoutMs;

    /**
     * Метод для начала процесса агрегации данных.
     * Подписывается на топики для получения событий от датчиков,
     * формирует снимок их состояния и записывает в кафку.
     */
    public void start() {
        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));

        try {
            consumer.subscribe(List.of(sensorsTopic));
            log.info("Подписались на топик: {}", sensorsTopic);

            while (true) {
                ConsumerRecords<String, SensorEventAvro> records =
                        consumer.poll(Duration.ofMillis(pollTimeoutMs));

                if (records.isEmpty()) {
                    continue;
                }

                for (ConsumerRecord<String, SensorEventAvro> record : records) {
                    log.debug("Получено событие датчика: key={}, partition={}, offset={}",
                            record.key(), record.partition(), record.offset());

                    Optional<SensorsSnapshotAvro> snapshotOpt =
                            snapshotService.updateState(record.value());

                    snapshotOpt.ifPresent(this::sendSnapshot);
                }

                producer.flush();
                consumer.commitSync();
            }

        } catch (WakeupException ignored) {
            // игнорируем - закрываем консьюмер и продюсер в блоке finally
        } catch (Exception e) {
            log.error("Ошибка во время обработки событий от датчиков", e);
        } finally {

            try {
                // Перед тем, как закрыть продюсер и консьюмер, нужно убедиться,
                // что все сообщения, лежащие в буффере, отправлены и
                // все оффсеты обработанных сообщений зафиксированы

                // здесь нужно вызвать метод продюсера для сброса данных в буффере
                // здесь нужно вызвать метод консьюмера для фиксации смещений
                producer.flush();
                consumer.commitSync();
            } finally {
                log.info("Закрываем консьюмер");
                consumer.close();
                log.info("Закрываем продюсер");
                producer.close();
            }
        }
    }

    private void sendSnapshot(SensorsSnapshotAvro snapshot) {
        ProducerRecord<String, SensorsSnapshotAvro> record = new ProducerRecord<>(
                snapshotsTopic,
                null,
                snapshot.getTimestamp().toEpochMilli(),
                snapshot.getHubId(),
                snapshot
        );

        producer.send(record, (metadata, exception) -> {
            if (exception != null) {
                log.error("Ошибка при отправке снапшота для хаба {}: {}",
                        snapshot.getHubId(), exception.getMessage(), exception);
            } else {
                log.info("Снапшот для хаба {} отправлен в топик {} [partition={}, offset={}]",
                        snapshot.getHubId(), metadata.topic(), metadata.partition(), metadata.offset());
            }
        });
    }
}