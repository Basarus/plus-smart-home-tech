package ru.yandex.practicum.telemetry.collector.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class ScenarioAddedEventDto extends HubEventDto {
    @NotBlank
    private String name;

    @NotEmpty
    private List<@Valid ScenarioConditionDto> conditions;

    @NotEmpty
    private List<@Valid DeviceActionDto> actions;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ScenarioConditionDto> getConditions() {
        return conditions;
    }

    public void setConditions(List<ScenarioConditionDto> conditions) {
        this.conditions = conditions;
    }

    public List<DeviceActionDto> getActions() {
        return actions;
    }

    public void setActions(List<DeviceActionDto> actions) {
        this.actions = actions;
    }
}