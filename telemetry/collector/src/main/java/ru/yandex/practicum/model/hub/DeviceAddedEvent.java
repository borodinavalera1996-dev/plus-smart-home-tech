package ru.yandex.practicum.model.hub;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Событие, сигнализирующее о добавлении нового устройства в систему.
 */
@Getter
@Setter
@ToString
@Schema(name = "DeviceAddedEvent", description = "Событие, сигнализирующее о добавлении нового устройства в систему.")
public class DeviceAddedEvent extends CollectHubEventRequest {

    private String id;
    private DeviceTypeEnum deviceType;

    @Override
    public HubEventType getType() {
        return HubEventType.DEVICE_ADDED;
    }

    /**
     * Перечисление типов устройств, которые могут быть добавлены в систему.
     */
    public enum DeviceTypeEnum {
        MOTION_SENSOR("MOTION_SENSOR"),

        TEMPERATURE_SENSOR("TEMPERATURE_SENSOR"),

        LIGHT_SENSOR("LIGHT_SENSOR"),

        CLIMATE_SENSOR("CLIMATE_SENSOR"),

        SWITCH_SENSOR("SWITCH_SENSOR");

        private String value;

        DeviceTypeEnum(String value) {
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
        public static DeviceTypeEnum fromValue(String value) {
            for (DeviceTypeEnum b : DeviceTypeEnum.values()) {
                if (b.value.equals(value)) {
                    return b;
                }
            }
            throw new IllegalArgumentException("Unexpected value '" + value + "'");
        }
    }
}

