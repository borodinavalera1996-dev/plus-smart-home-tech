package ru.yandex.practicum.service.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.jpa.ScenarioRepository;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioRemovedEventAvro;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScenarioRemovedEventHandler implements HubEventHandler<ScenarioRemovedEventAvro> {

    private final ScenarioRepository scenarioRepository;

    @Override
    public Class<ScenarioRemovedEventAvro> getPayloadType() {
        return ScenarioRemovedEventAvro.class;
    }

    @Override
    @Transactional
    public void handle(String hubId, ScenarioRemovedEventAvro payload) {
        scenarioRepository.deleteByHubIdAndName(hubId, payload.getName());
        log.info("Удалён сценарий '{}' для хаба {}", payload.getName(), hubId);
    }
}

