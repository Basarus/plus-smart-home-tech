package ru.yandex.practicum.telemetry.collector.api.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.time.Instant;

public record ScenarioRemovedEventDto(
        @JsonAlias({
                "hubId", "hub_id" }) String hubId,
        Instant timestamp,
        String name) implements HubEventDto {
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
}