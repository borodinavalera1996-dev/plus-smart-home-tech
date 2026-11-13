package ru.yandex.practicum.model.hub;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Событие удаления сценария из системы. Содержит информацию о названии удаленного сценария.
 */
@Getter
@Setter
@ToString
@Schema(name = "ScenarioRemovedEvent", description = "Событие удаления сценария из системы. Содержит информацию о названии удаленного сценария.")
public class ScenarioRemovedEvent extends CollectHubEventRequest {

    private String name;

    @Override
    public HubEventType getType() {
        return HubEventType.SCENARIO_REMOVED;
    }
}

