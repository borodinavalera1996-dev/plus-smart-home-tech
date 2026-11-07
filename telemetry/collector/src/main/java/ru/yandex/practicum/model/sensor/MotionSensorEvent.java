package ru.yandex.practicum.model.sensor;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Событие датчика движения.
 */
@Getter
@Setter
@ToString
@Schema(name = "MotionSensorEvent", description = "Событие датчика движения.")
public class MotionSensorEvent extends CollectSensorEventRequest {

    private Integer linkQuality;

    private Boolean motion;

    private Integer voltage;

    @Override
    public SensorEventType getType() {
        return SensorEventType.MOTION_SENSOR_EVENT;
    }
}

