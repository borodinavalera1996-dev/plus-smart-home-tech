package ru.yandex.practicum.model.sensor;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Событие датчика температуры, содержащее информацию о температуре в градусах Цельсия и Фаренгейта.
 */
@Getter
@Setter
@ToString
@Schema(name = "TemperatureSensorEvent", description = "Событие датчика температуры, содержащее информацию о температуре в градусах Цельсия и Фаренгейта.")
public class TemperatureSensorEvent extends CollectSensorEventRequest {

    private Integer temperatureC;

    private Integer temperatureF;

    @Override
    public SensorEventType getType() {
        return SensorEventType.SWITCH_SENSOR_EVENT;
    }
}

