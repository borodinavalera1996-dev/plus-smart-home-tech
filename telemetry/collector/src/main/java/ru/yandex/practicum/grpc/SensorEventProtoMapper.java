package ru.yandex.practicum.grpc;

import com.google.protobuf.Timestamp;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.ClimateSensorProto;
import ru.yandex.practicum.grpc.telemetry.event.LightSensorProto;
import ru.yandex.practicum.grpc.telemetry.event.MotionSensorProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.grpc.telemetry.event.SwitchSensorProto;
import ru.yandex.practicum.grpc.telemetry.event.TemperatureSensorProto;
import ru.yandex.practicum.model.sensor.ClimateSensorEvent;
import ru.yandex.practicum.model.sensor.CollectSensorEventRequest;
import ru.yandex.practicum.model.sensor.LightSensorEvent;
import ru.yandex.practicum.model.sensor.MotionSensorEvent;
import ru.yandex.practicum.model.sensor.SwitchSensorEvent;
import ru.yandex.practicum.model.sensor.TemperatureSensorEvent;

import java.time.Instant;

@Component
public class SensorEventProtoMapper {

    public CollectSensorEventRequest toModel(SensorEventProto proto) {
        CollectSensorEventRequest request = switch (proto.getPayloadCase()) {
            case MOTION_SENSOR -> mapMotionSensor(proto.getMotionSensor());
            case TEMPERATURE_SENSOR -> mapTemperatureSensor(proto.getTemperatureSensor());
            case LIGHT_SENSOR -> mapLightSensor(proto.getLightSensor());
            case CLIMATE_SENSOR -> mapClimateSensor(proto.getClimateSensor());
            case SWITCH_SENSOR -> mapSwitchSensor(proto.getSwitchSensor());
            case PAYLOAD_NOT_SET -> throw new IllegalArgumentException(
                    "Не задан payload для события датчика: id=" + proto.getId()
            );
        };

        request.setId(proto.getId());
        request.setHubId(proto.getHubId());
        request.setTimestamp(mapTimestamp(proto.getTimestamp()));

        return request;
    }

    private MotionSensorEvent mapMotionSensor(MotionSensorProto proto) {
        MotionSensorEvent event = new MotionSensorEvent();
        event.setLinkQuality(proto.getLinkQuality());
        event.setMotion(proto.getMotion());
        event.setVoltage(proto.getVoltage());
        return event;
    }

    private TemperatureSensorEvent mapTemperatureSensor(TemperatureSensorProto proto) {
        TemperatureSensorEvent event = new TemperatureSensorEvent();
        event.setTemperatureC(proto.getTemperatureC());
        event.setTemperatureF(proto.getTemperatureF());
        return event;
    }

    private LightSensorEvent mapLightSensor(LightSensorProto proto) {
        LightSensorEvent event = new LightSensorEvent();
        event.setLinkQuality(proto.getLinkQuality());
        event.setLuminosity(proto.getLuminosity());
        return event;
    }

    private ClimateSensorEvent mapClimateSensor(ClimateSensorProto proto) {
        ClimateSensorEvent event = new ClimateSensorEvent();
        event.setTemperatureC(proto.getTemperatureC());
        event.setHumidity(proto.getHumidity());
        event.setCo2Level(proto.getCo2Level());
        return event;
    }

    private SwitchSensorEvent mapSwitchSensor(SwitchSensorProto proto) {
        SwitchSensorEvent event = new SwitchSensorEvent();
        event.setState(proto.getState());
        return event;
    }

    private Instant mapTimestamp(Timestamp timestamp) {
        if (timestamp == null || (timestamp.getSeconds() == 0 && timestamp.getNanos() == 0)) {
            return Instant.now();
        }
        return Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
    }
}
