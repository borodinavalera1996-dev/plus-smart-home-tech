package ru.yandex.practicum.model.hub;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * Событие добавления сценария в систему. Содержит информацию о названии сценария, условиях и действиях.
 */
@Getter
@Setter
@ToString
@Schema(name = "ScenarioAddedEvent", description = "Событие добавления сценария в систему. Содержит информацию о названии сценария, условиях и действиях.")
public class ScenarioAddedEvent extends CollectHubEventRequest {

    private String name;

    @Valid
    private List<@Valid ScenarioCondition> conditions = new ArrayList<>();

    @Valid
    private List<@Valid DeviceAction> actions = new ArrayList<>();

    @Override
    public HubEventType getType() {
        return HubEventType.SCENARIO_ADDED;
    }
}

