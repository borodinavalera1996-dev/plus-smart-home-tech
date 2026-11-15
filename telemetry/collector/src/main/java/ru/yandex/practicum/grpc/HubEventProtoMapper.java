package ru.yandex.practicum.grpc;

import com.google.protobuf.Timestamp;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.*;
import ru.yandex.practicum.model.hub.*;

import java.time.Instant;
import java.util.List;

@Component
public class HubEventProtoMapper {

    public CollectHubEventRequest toModel(HubEventProto proto) {
        CollectHubEventRequest request = switch (proto.getPayloadCase()) {
            case DEVICE_ADDED -> mapDeviceAdded(proto.getDeviceAdded());
            case DEVICE_REMOVED -> mapDeviceRemoved(proto.getDeviceRemoved());
            case SCENARIO_ADDED -> mapScenarioAdded(proto.getScenarioAdded());
            case SCENARIO_REMOVED -> mapScenarioRemoved(proto.getScenarioRemoved());
            case PAYLOAD_NOT_SET -> throw new IllegalArgumentException(
                    "Не задан payload для события хаба: hubId=" + proto.getHubId()
            );
        };

        request.setHubId(proto.getHubId());
        request.setTimestamp(mapTimestamp(proto.getTimestamp()));

        return request;
    }

    private DeviceAddedEvent mapDeviceAdded(DeviceAddedEventProto proto) {
        DeviceAddedEvent event = new DeviceAddedEvent();
        event.setId(proto.getId());
        event.setDeviceType(mapDeviceType(proto.getType()));
        return event;
    }

    private DeviceRemovedEvent mapDeviceRemoved(DeviceRemovedEventProto proto) {
        DeviceRemovedEvent event = new DeviceRemovedEvent();
        event.setId(proto.getId());
        return event;
    }

    private ScenarioAddedEvent mapScenarioAdded(ScenarioAddedEventProto proto) {
        ScenarioAddedEvent event = new ScenarioAddedEvent();
        event.setName(proto.getId());

        List<ScenarioCondition> conditions = proto.getConditionsList().stream()
                .map(this::mapScenarioCondition)
                .toList();
        event.setConditions(conditions);

        List<DeviceAction> actions = proto.getActionList().stream()
                .map(this::mapDeviceAction)
                .toList();
        event.setActions(actions);

        return event;
    }

    private ScenarioRemovedEvent mapScenarioRemoved(ScenarioRemovedEventProto proto) {
        ScenarioRemovedEvent event = new ScenarioRemovedEvent();
        event.setName(proto.getId());
        return event;
    }

    private DeviceAddedEvent.DeviceTypeEnum mapDeviceType(DeviceTypeProto protoType) {
        return switch (protoType) {
            case MOTION_SENSOR -> DeviceAddedEvent.DeviceTypeEnum.MOTION_SENSOR;
            case TEMPERATURE_SENSOR -> DeviceAddedEvent.DeviceTypeEnum.TEMPERATURE_SENSOR;
            case LIGHT_SENSOR -> DeviceAddedEvent.DeviceTypeEnum.LIGHT_SENSOR;
            case CLIMATE_SENSOR -> DeviceAddedEvent.DeviceTypeEnum.CLIMATE_SENSOR;
            case SWITCH_SENSOR -> DeviceAddedEvent.DeviceTypeEnum.SWITCH_SENSOR;
            case UNRECOGNIZED -> throw new IllegalArgumentException("Неизвестный тип устройства: " + protoType);
        };
    }

    private ScenarioCondition mapScenarioCondition(ScenarioConditionProto proto) {
        ScenarioCondition condition = new ScenarioCondition();
        condition.setSensorId(proto.getSensorId());
        condition.setType(mapConditionType(proto.getType()));
        condition.setOperation(mapConditionOperation(proto.getOperation()));

        Integer value = switch (proto.getValueCase()) {
            case BOOL_VALUE -> proto.getBoolValue() ? 1 : 0;
            case INT_VALUE -> proto.getIntValue();
            case VALUE_NOT_SET -> null;
        };
        condition.setValue(value);

        return condition;
    }

    private DeviceAction mapDeviceAction(DeviceActionProto proto) {
        DeviceAction action = new DeviceAction();
        action.setSensorId(proto.getSensorId());
        action.setType(mapActionType(proto.getType()));

        if (proto.hasValue()) {
            action.setValue(proto.getValue());
        }

        return action;
    }

    private ScenarioCondition.TypeEnum mapConditionType(ConditionTypeProto protoType) {
        return switch (protoType) {
            case MOTION -> ScenarioCondition.TypeEnum.MOTION;
            case LUMINOSITY -> ScenarioCondition.TypeEnum.LUMINOSITY;
            case SWITCH -> ScenarioCondition.TypeEnum.SWITCH;
            case TEMPERATURE -> ScenarioCondition.TypeEnum.TEMPERATURE;
            case CO2LEVEL -> ScenarioCondition.TypeEnum.CO2_LEVEL;
            case HUMIDITY -> ScenarioCondition.TypeEnum.HUMIDITY;
            case UNRECOGNIZED -> throw new IllegalArgumentException("Неизвестный тип условия: " + protoType);
        };
    }

    private ScenarioCondition.OperationEnum mapConditionOperation(ConditionOperationProto protoOp) {
        return switch (protoOp) {
            case EQUALS -> ScenarioCondition.OperationEnum.EQUALS;
            case GREATER_THAN -> ScenarioCondition.OperationEnum.GREATER_THAN;
            case LOWER_THAN -> ScenarioCondition.OperationEnum.LOWER_THAN;
            case UNRECOGNIZED -> throw new IllegalArgumentException("Неизвестная операция условия: " + protoOp);
        };
    }

    private DeviceAction.TypeEnum mapActionType(ActionTypeProto protoType) {
        return switch (protoType) {
            case ACTIVATE -> DeviceAction.TypeEnum.ACTIVATE;
            case DEACTIVATE -> DeviceAction.TypeEnum.DEACTIVATE;
            case INVERSE -> DeviceAction.TypeEnum.INVERSE;
            case SET_VALUE -> DeviceAction.TypeEnum.SET_VALUE;
            case UNRECOGNIZED -> throw new IllegalArgumentException("Неизвестный тип действия: " + protoType);
        };
    }


    private Instant mapTimestamp(Timestamp timestamp) {
        if (timestamp == null || (timestamp.getSeconds() == 0 && timestamp.getNanos() == 0)) {
            return Instant.now();
        }
        return Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
    }
}
