package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.model.hub.*;
import ru.yandex.practicum.kafka.TelemetryKafkaProducer;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HubService {

    private final TelemetryKafkaProducer kafkaProducer;

    public void processHubEvent(CollectHubEventRequest request) {
        log.info("Обработка события хаба: hubId={}", request.getHubId());

        Object avroPayload = switch (request.getType()) {
            case DEVICE_ADDED -> {
                var dto = (DeviceAddedEvent) request;
                yield DeviceAddedEventAvro.newBuilder()
                        .setId(dto.getId())
                        .setType(DeviceTypeAvro.valueOf(dto.getDeviceType().name()))
                        .build();
            }
            case DEVICE_REMOVED -> {
                var dto = (DeviceRemovedEvent) request;
                yield DeviceRemovedEventAvro.newBuilder()
                        .setId(dto.getId())
                        .build();
            }
            case SCENARIO_ADDED -> {
                var dto = (ScenarioAddedEvent) request;
                yield ScenarioAddedEventAvro.newBuilder()
                        .setName(dto.getName())
                        .setConditions(mapConditions(dto.getConditions()))
                        .setActions(mapActions(dto.getActions()))
                        .build();
            }
            case SCENARIO_REMOVED -> {
                var dto = (ScenarioRemovedEvent) request;
                yield ScenarioRemovedEventAvro.newBuilder()
                        .setName(dto.getName())
                        .build();
            }
            default -> throw new IllegalArgumentException("Неизвестное событие хаба: " + request.getType().getClass());
        };

        HubEventAvro hubEvent = HubEventAvro.newBuilder()
                .setHubId(request.getHubId())
                .setTimestamp(request.getTimestamp())
                .setPayload(avroPayload)
                .build();
        kafkaProducer.sendHubEvent(hubEvent);
    }

    private List<ScenarioConditionAvro> mapConditions(List<ScenarioCondition> dtos) {
        if (dtos == null) return List.of();
        return dtos.stream().map(dto -> ScenarioConditionAvro.newBuilder()
                .setSensorId(dto.getSensorId())
                .setType(ConditionTypeAvro.valueOf(dto.getType().name()))
                .setOperation(ConditionOperationAvro.valueOf(dto.getOperation().name()))
                .setValue(dto.getValue())
                .build()
        ).collect(Collectors.toList());
    }

    private List<DeviceActionAvro> mapActions(List<DeviceAction> dtos) {
        if (dtos == null) return List.of();
        return dtos.stream().map(dto -> DeviceActionAvro.newBuilder()
                .setSensorId(dto.getSensorId())
                .setType(ActionTypeAvro.valueOf(dto.getType().name()))
                .setValue(dto.getValue())
                .build()
        ).collect(Collectors.toList());
    }
}
