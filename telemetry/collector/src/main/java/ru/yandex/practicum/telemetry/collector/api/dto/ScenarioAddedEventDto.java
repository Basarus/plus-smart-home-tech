package ru.yandex.practicum.telemetry.collector.api.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.time.Instant;
import java.util.List;

public record ScenarioAddedEventDto(
        @JsonAlias({
                "hubId", "hub_id" }) String hubId,
        Instant timestamp,
        String name,
        List<ScenarioConditionDto> conditions,
        List<DeviceActionDto> actions) implements HubEventDto {
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