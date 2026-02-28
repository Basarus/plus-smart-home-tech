package ru.yandex.practicum.telemetry.collector.api.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record ScenarioRemovedEventDto(

        @NotBlank @JsonAlias({
                "hubId", "hub_id" }) String hubId,

        @NotNull Instant timestamp,

        @NotBlank String name

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
}