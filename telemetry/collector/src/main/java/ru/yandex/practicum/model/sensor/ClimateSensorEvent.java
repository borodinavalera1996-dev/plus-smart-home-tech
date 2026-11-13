package ru.yandex.practicum.model.sensor;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Событие климатического датчика, содержащее информацию о температуре, влажности и уровне CO2.
 */
@Getter
@Setter
@ToString
@Schema(name = "ClimateSensorEvent", description = "Событие климатического датчика, содержащее информацию о температуре, влажности и уровне CO2.")
public class ClimateSensorEvent extends CollectSensorEventRequest {

    private Integer temperatureC;

    private Integer humidity;

    private Integer co2Level;

    @Override
    public SensorEventType getType() {
        return SensorEventType.CLIMATE_SENSOR_EVENT;
    }
}

