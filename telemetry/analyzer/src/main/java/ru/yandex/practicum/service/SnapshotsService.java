package ru.yandex.practicum.service;

import com.google.protobuf.Timestamp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.api.HubRouterSender;
import ru.yandex.practicum.grpc.telemetry.event.ActionTypeProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;
import ru.yandex.practicum.kafka.telemetry.event.ActionTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.ConditionTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.LightSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.MotionSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.kafka.telemetry.event.SwitchSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.TemperatureSensorAvro;
import ru.yandex.practicum.jpa.model.Action;
import ru.yandex.practicum.jpa.model.Condition;
import ru.yandex.practicum.jpa.model.Scenario;
import ru.yandex.practicum.jpa.ScenarioRepository;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SnapshotsService {

    private final HubRouterSender hubRouterSender;
    private final ScenarioRepository scenarioRepository;

    @Transactional(readOnly = true)
    public void process(SensorsSnapshotAvro snapshot) {
        String hubId = snapshot.getHubId();
        Map<String, SensorStateAvro> sensorsState = snapshot.getSensorsState();

        if (sensorsState == null || sensorsState.isEmpty()) {
            log.debug("Снапшот для хаба {} не содержит состояний датчиков", hubId);
            return;
        }

        List<Scenario> scenarios = scenarioRepository.findByHubId(hubId);
        if (scenarios.isEmpty()) {
            log.debug("Для хаба {} не найдено ни одного сценария", hubId);
            return;
        }

        log.debug("Анализ снапшота для хаба {}: найдено {} сценариев", hubId, scenarios.size());

        for (Scenario scenario : scenarios) {
            if (isScenarioTriggered(scenario, sensorsState)) {
                log.info("Сработал сценарий '{}' для хаба {}", scenario.getName(), hubId);
                executeActions(scenario, snapshot.getTimestamp());
            }
        }
    }

    private boolean isScenarioTriggered(Scenario scenario, Map<String, SensorStateAvro> sensorsState) {
        Map<String, Condition> conditions = scenario.getConditions();
        if (conditions == null || conditions.isEmpty()) {
            return false;
        }

        for (Map.Entry<String, Condition> entry : conditions.entrySet()) {
            String sensorId = entry.getKey();
            Condition condition = entry.getValue();

            SensorStateAvro sensorState = sensorsState.get(sensorId);
            if (sensorState == null) {
                return false;
            }

            if (!checkCondition(condition, sensorState)) {
                return false;
            }
        }

        return true;
    }

    private boolean checkCondition(Condition condition, SensorStateAvro sensorState) {
        Integer actualValue = extractSensorValue(condition.getType(), sensorState.getData());
        Integer targetValue = condition.getValue();

        if (actualValue == null || targetValue == null) {
            return false;
        }

        return switch (condition.getOperation()) {
            case EQUALS -> actualValue.equals(targetValue);
            case GREATER_THAN -> actualValue > targetValue;
            case LOWER_THAN -> actualValue < targetValue;
        };
    }

    private Integer extractSensorValue(ConditionTypeAvro type, Object data) {
        if (data == null) {
            return null;
        }

        return switch (type) {
            case MOTION -> (data instanceof MotionSensorAvro m) ? (m.getMotion() ? 1 : 0) : null;
            case SWITCH -> (data instanceof SwitchSensorAvro s) ? (s.getState() ? 1 : 0) : null;
            case LUMINOSITY -> (data instanceof LightSensorAvro l) ? l.getLuminosity() : null;
            case CO2LEVEL -> (data instanceof ClimateSensorAvro c) ? c.getCo2Level() : null;
            case HUMIDITY -> (data instanceof ClimateSensorAvro c) ? c.getHumidity() : null;
            case TEMPERATURE -> {
                if (data instanceof TemperatureSensorAvro t) {
                    yield t.getTemperatureC();
                } else if (data instanceof ClimateSensorAvro c) {
                    yield c.getTemperatureC();
                }
                yield null;
            }
        };
    }

    private void executeActions(Scenario scenario, Instant snapshotTimestamp) {
        Map<String, Action> actions = scenario.getActions();
        if (actions == null || actions.isEmpty()) {
            return;
        }

        Instant actionTime = (snapshotTimestamp != null) ? snapshotTimestamp : Instant.now();
        Timestamp protoTimestamp = Timestamp.newBuilder()
                .setSeconds(actionTime.getEpochSecond())
                .setNanos(actionTime.getNano())
                .build();

        for (Map.Entry<String, Action> entry : actions.entrySet()) {
            String sensorId = entry.getKey();
            Action action = entry.getValue();

            DeviceActionProto.Builder actionBuilder = DeviceActionProto.newBuilder()
                    .setSensorId(sensorId)
                    .setType(mapActionType(action.getType()));

            if (action.getValue() != null) {
                actionBuilder.setValue(action.getValue());
            }

            DeviceActionRequest request = DeviceActionRequest.newBuilder()
                    .setHubId(scenario.getHubId())
                    .setScenarioName(scenario.getName())
                    .setAction(actionBuilder.build())
                    .setTimestamp(protoTimestamp)
                    .build();

            hubRouterSender.sendAction(request);
        }
    }

    private ActionTypeProto mapActionType(ActionTypeAvro avroType) {
        return switch (avroType) {
            case ACTIVATE -> ActionTypeProto.ACTIVATE;
            case DEACTIVATE -> ActionTypeProto.DEACTIVATE;
            case INVERSE -> ActionTypeProto.INVERSE;
            case SET_VALUE -> ActionTypeProto.SET_VALUE;
        };
    }
}
