package ru.yandex.practicum.telemetry.collector.api.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.time.Instant;

public record TemperatureSensorEventDto(
        String id,
        String hubId,
        Instant timestamp,
        @JsonAlias("temperature_c") int temperatureC,
        @JsonAlias("temperature_f") int temperatureF) implements SensorEventDto {
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