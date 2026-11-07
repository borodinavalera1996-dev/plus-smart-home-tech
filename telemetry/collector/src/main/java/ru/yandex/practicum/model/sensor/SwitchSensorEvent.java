package ru.yandex.practicum.model.sensor;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Событие датчика переключателя, содержащее информацию о текущем состоянии переключателя.
 */
@Getter
@Setter
@ToString
@Schema(name = "SwitchSensorEvent", description = "Событие датчика переключателя, содержащее информацию о текущем состоянии переключателя.")
public class SwitchSensorEvent extends CollectSensorEventRequest {

    private Boolean state;

    @Override
    public SensorEventType getType() {
        return SensorEventType.SWITCH_SENSOR_EVENT;
    }
}

