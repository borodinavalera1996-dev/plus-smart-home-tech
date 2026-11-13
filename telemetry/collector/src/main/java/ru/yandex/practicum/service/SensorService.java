package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.TelemetryKafkaProducer;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.model.sensor.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SensorService {

    private final TelemetryKafkaProducer kafkaProducer;

    public void processSensorEvent(CollectSensorEventRequest request) {
        log.info("Обработка события датчика: id={}, hubId={}", request.getId(), request.getHubId());

        Object avroPayload = switch (request.getType()) {
            case CLIMATE_SENSOR_EVENT -> {
                var dto = (ClimateSensorEvent) request;
                yield ClimateSensorAvro.newBuilder()
                        .setTemperatureC(dto.getTemperatureC())
                        .setHumidity(dto.getHumidity())
                        .setCo2Level(dto.getCo2Level())
                        .build();
            }
            case LIGHT_SENSOR_EVENT -> {
                var dto = (LightSensorEvent) request;
                yield LightSensorAvro.newBuilder()
                        .setLinkQuality(dto.getLinkQuality())
                        .setLuminosity(dto.getLuminosity())
                        .build();
            }
            case MOTION_SENSOR_EVENT -> {
                var dto = (MotionSensorEvent) request;
                yield MotionSensorAvro.newBuilder()
                        .setLinkQuality(dto.getLinkQuality())
                        .setMotion(dto.getMotion())
                        .setVoltage(dto.getVoltage())
                        .build();
            }
            case SWITCH_SENSOR_EVENT -> {
                var dto = (SwitchSensorEvent) request;
                yield SwitchSensorAvro.newBuilder()
                        .setState(dto.getState())
                        .build();
            }
            case TEMPERATURE_SENSOR_EVENT -> {
                var dto = (TemperatureSensorEvent) request;
                yield TemperatureSensorAvro.newBuilder()
                        .setId(dto.getId())
                        .setHubId(dto.getHubId())
                        .setTimestamp(dto.getTimestamp())
                        .setTemperatureC(dto.getTemperatureC())
                        .setTemperatureF(dto.getTemperatureF())
                        .build();
            }
            default -> throw new IllegalArgumentException("Неизвестный тип датчика: " + request.getType().getClass());
        };

        SensorEventAvro sensorEvent = SensorEventAvro.newBuilder()
                .setId(request.getId())
                .setHubId(request.getHubId())
                .setTimestamp(request.getTimestamp())
                .setPayload(avroPayload)
                .build();

        kafkaProducer.sendSensorEvent(sensorEvent);
    }
}

