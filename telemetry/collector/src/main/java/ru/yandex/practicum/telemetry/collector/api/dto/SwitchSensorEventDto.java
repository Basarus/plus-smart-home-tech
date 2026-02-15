package ru.yandex.practicum.telemetry.collector.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record SwitchSensorEventDto(

        @NotBlank String id,

        @NotBlank String hubId,

        @NotNull Instant timestamp,

        @NotNull Boolean state

) implements SensorEventDto {

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getHubId() {
        return hubId;
    }

    @Override
    public Instant getTimestamp() {
        return timestamp;
    }
}