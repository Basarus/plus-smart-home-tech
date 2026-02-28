package ru.yandex.practicum.telemetry.collector.api.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record MotionSensorEventDto(

        @NotBlank String id,

        @NotBlank String hubId,

        @NotNull Instant timestamp,

        @NotNull @JsonAlias("link_quality") Integer linkQuality,

        @NotNull Boolean motion,

        @NotNull Integer voltage

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