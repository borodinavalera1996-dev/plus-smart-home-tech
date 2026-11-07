package ru.yandex.practicum.model.sensor;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Перечисление типов событий датчиков. Определяет различные типы событий, которые могут быть связаны с датчиками.
 */
public enum SensorEventType {
    MOTION_SENSOR_EVENT("MOTION_SENSOR_EVENT"),

    TEMPERATURE_SENSOR_EVENT("TEMPERATURE_SENSOR_EVENT"),

    LIGHT_SENSOR_EVENT("LIGHT_SENSOR_EVENT"),

    CLIMATE_SENSOR_EVENT("CLIMATE_SENSOR_EVENT"),

    SWITCH_SENSOR_EVENT("SWITCH_SENSOR_EVENT");

    private String value;

    SensorEventType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static SensorEventType fromValue(String value) {
        for (SensorEventType b : SensorEventType.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}
