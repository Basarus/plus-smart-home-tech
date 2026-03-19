package ru.yandex.practicum.telemetry.collector.api.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.google.protobuf.Timestamp;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;

public record ScenarioAddedEventDto(
        @JsonAlias({
                "hubId", "hub_id" }) @NotBlank String hubId,

        @NotNull Timestamp timestamp,

        @NotBlank String name,

        @NotNull @Valid List<ScenarioConditionDto> conditions,

        @NotNull @Valid List<DeviceActionDto> actions

    ) implements HubEventDto {

    @Override
    public String getHubId() {
        return hubId;
    }

    @Override
    public Timestamp getTimestamp() {
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