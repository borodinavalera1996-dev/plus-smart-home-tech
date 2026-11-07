package ru.yandex.practicum.model.hub;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Перечисление типов устройств, которые могут быть добавлены в систему.
 */
public enum HubEventType {
    DEVICE_ADDED("DEVICE_ADDED"),

    DEVICE_REMOVED("DEVICE_REMOVED"),

    SCENARIO_ADDED("SCENARIO_ADDED"),

    SCENARIO_REMOVED("SCENARIO_REMOVED");

    private String value;

    HubEventType(String value) {
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
    public static HubEventType fromValue(String value) {
        for (HubEventType b : HubEventType.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}
