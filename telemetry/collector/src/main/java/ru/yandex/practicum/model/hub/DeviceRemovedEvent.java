package ru.yandex.practicum.model.hub;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Событие, сигнализирующее о удалении устройства из системы.
 */
@Getter
@Setter
@ToString
@Schema(name = "DeviceRemovedEvent", description = "Событие, сигнализирующее о удалении устройства из системы.")
public class DeviceRemovedEvent extends CollectHubEventRequest {
    private String id;

    @Override
    public HubEventType getType() {
        return HubEventType.DEVICE_REMOVED;
    }
}

