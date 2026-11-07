package ru.yandex.practicum.kafka;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
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

    @Bean
    public ProducerFactory<String, SensorEventAvro> sensorProducerFactory() {
        return new DefaultKafkaProducerFactory<>(baseProducerConfigs());
    }

    @Bean
    public KafkaTemplate<String, SensorEventAvro> sensorKafkaTemplate() {
        return new KafkaTemplate<>(sensorProducerFactory());
    }

    @Bean
    public ProducerFactory<String, HubEventAvro> hubProducerFactory() {
        return new DefaultKafkaProducerFactory<>(baseProducerConfigs());
    }

    @Bean
    public KafkaTemplate<String, HubEventAvro> hubKafkaTemplate() {
        return new KafkaTemplate<>(hubProducerFactory());
    }
}

