package ru.yandex.practicum.service.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.jpa.ScenarioRepository;
import ru.yandex.practicum.jpa.SensorRepository;
import ru.yandex.practicum.jpa.model.Scenario;
import ru.yandex.practicum.kafka.telemetry.event.DeviceRemovedEventAvro;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeviceRemovedEventHandler implements HubEventHandler<DeviceRemovedEventAvro> {

    private final SensorRepository sensorRepository;
    private final ScenarioRepository scenarioRepository;

    @Override
    public Class<DeviceRemovedEventAvro> getPayloadType() {
        return DeviceRemovedEventAvro.class;
    }

    @Override
    @Transactional
    public void handle(String hubId, DeviceRemovedEventAvro payload) {
        String sensorId = payload.getId();

        List<Scenario> scenarios = scenarioRepository.findByHubId(hubId);
        for (Scenario scenario : scenarios) {
            boolean changed = scenario.getConditions().remove(sensorId) != null;
            if (scenario.getActions().remove(sensorId) != null) {
                changed = true;
            }
            if (changed) {
                scenarioRepository.save(scenario);
            }
        }

        sensorRepository.deleteByIdAndHubId(sensorId, hubId);
        log.info("Удалено устройство id={} для хаба {}", sensorId, hubId);
    }
}

