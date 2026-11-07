package ru.yandex.practicum.model.sensor;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Событие датчика освещенности, содержащее информацию о качестве связи и уровне освещенности.
 */
@Getter
@Setter
@ToString
@Schema(name = "LightSensorEvent", description = "Событие датчика освещенности, содержащее информацию о качестве связи и уровне освещенности.")
public class LightSensorEvent extends CollectSensorEventRequest {

    private Integer linkQuality;

    private Integer luminosity;

    @Override
    public SensorEventType getType() {
        return SensorEventType.LIGHT_SENSOR_EVENT;
    }
}

