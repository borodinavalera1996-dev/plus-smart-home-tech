package ru.yandex.practicum.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.producer.properties.schema.registry.url}")
    private String schemaRegistryUrl;

    private Map<String, Object> baseProducerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, AvroSerializer.class);

        props.put("schema.registry.url", schemaRegistryUrl);
        props.put("auto.register.schemas", true);
        return props;
    }

    @Bean(destroyMethod = "close")
    public Producer<String, SensorEventAvro> sensorProducer() {
        return new KafkaProducer<>(baseProducerConfigs());
    }

    @Bean(destroyMethod = "close")
    public Producer<String, HubEventAvro> hubProducer() {
        return new KafkaProducer<>(baseProducerConfigs());
    }
}

