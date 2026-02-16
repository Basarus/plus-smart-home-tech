package ru.yandex.practicum.telemetry.collector.api.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;

public record ScenarioAddedEventDto(
        @JsonAlias({
                "hubId", "hub_id" }) @NotBlank String hubId,

        @NotNull Instant timestamp,

        @NotBlank String name,

        @NotNull @Valid List<ScenarioConditionDto> conditions,

        @NotNull @Valid List<DeviceActionDto> actions
        
    ) implements HubEventDto {

    @Override
    public String getHubId() {
        return hubId;
    }

    @Override
    public Instant getTimestamp() {
        return timestamp;
    }

    public String getName() {
        return name;
    }

    public List<ScenarioConditionDto> getConditions() {
        return conditions;
    }

    public List<DeviceActionDto> getActions() {
        return actions;
    }
}