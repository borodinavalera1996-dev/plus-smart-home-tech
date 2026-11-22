package ru.yandex.practicum.service.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.jpa.SensorRepository;
import ru.yandex.practicum.jpa.model.Sensor;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeviceAddedEventHandler implements HubEventHandler<DeviceAddedEventAvro> {

    private final SensorRepository sensorRepository;

    @Override
    public Class<DeviceAddedEventAvro> getPayloadType() {
        return DeviceAddedEventAvro.class;
    }

    @Override
    @Transactional
    public void handle(String hubId, DeviceAddedEventAvro payload) {
        Sensor sensor = Sensor.builder()
                .id(payload.getId())
                .hubId(hubId)
                .build();
        sensorRepository.save(sensor);
        log.info("Добавлено устройство id={} для хаба {}", payload.getId(), hubId);
    }
}
