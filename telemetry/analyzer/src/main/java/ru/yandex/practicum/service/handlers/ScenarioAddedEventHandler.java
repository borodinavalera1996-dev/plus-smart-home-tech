package ru.yandex.practicum.service.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.jpa.ScenarioRepository;
import ru.yandex.practicum.jpa.SensorRepository;
import ru.yandex.practicum.jpa.model.Action;
import ru.yandex.practicum.jpa.model.Condition;
import ru.yandex.practicum.jpa.model.Scenario;
import ru.yandex.practicum.kafka.telemetry.event.DeviceActionAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioConditionAvro;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScenarioAddedEventHandler implements HubEventHandler<ScenarioAddedEventAvro> {

    private final SensorRepository sensorRepository;
    private final ScenarioRepository scenarioRepository;

    @Override
    public Class<ScenarioAddedEventAvro> getPayloadType() {
        return ScenarioAddedEventAvro.class;
    }

    @Override
    @Transactional
    public void handle(String hubId, ScenarioAddedEventAvro payload) {
        Set<String> sensorIds = new HashSet<>();
        payload.getConditions().forEach(c -> sensorIds.add(c.getSensorId()));
        payload.getActions().forEach(a -> sensorIds.add(a.getSensorId()));

        if (!sensorRepository.existsByIdInAndHubId(sensorIds, hubId)) {
            throw new IllegalArgumentException(
                    "Не все датчики из сценария '" + payload.getName() + "' принадлежат хабу " + hubId
            );
        }

        Scenario scenario = scenarioRepository.findByHubIdAndName(hubId, payload.getName())
                .orElseGet(() -> Scenario.builder()
                        .hubId(hubId)
                        .name(payload.getName())
                        .build());

        scenario.getConditions().clear();
        scenario.getActions().clear();

        Map<String, Condition> conditions = payload.getConditions().stream()
                .collect(Collectors.toMap(
                        ScenarioConditionAvro::getSensorId,
                        this::mapToCondition
                ));
        scenario.getConditions().putAll(conditions);

        Map<String, Action> actions = payload.getActions().stream()
                .collect(Collectors.toMap(
                        DeviceActionAvro::getSensorId,
                        this::mapToAction
                ));
        scenario.getActions().putAll(actions);

        scenarioRepository.save(scenario);
        log.info("Сохранён сценарий '{}' для хаба {}", payload.getName(), hubId);
    }

    private Condition mapToCondition(ScenarioConditionAvro avro) {
        Integer intValue = switch (avro.getValue()) {
            case Integer i -> i;
            case Boolean b -> b ? 1 : 0;
            case null -> null;
            default -> throw new IllegalArgumentException("Неподдерживаемый тип значения: " + avro.getValue());
        };

        return Condition.builder()
                .type(avro.getType())
                .operation(avro.getOperation())
                .value(intValue)
                .build();
    }

    private Action mapToAction(DeviceActionAvro avro) {
        return Action.builder()
                .type(avro.getType())
                .value(avro.getValue())
                .build();
    }
}

