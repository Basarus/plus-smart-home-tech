package ru.yandex.practicum.telemetry.collector.api.dto;

import jakarta.validation.constraints.NotBlank;

public class ScenarioRemovedEventDto extends HubEventDto {
    @NotBlank
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}