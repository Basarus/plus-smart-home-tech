package ru.yandex.practicum.telemetry.collector.api.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.time.Instant;

public record ClimateSensorEventDto(
        String id,
        String hubId,
        Instant timestamp,
        @JsonAlias("temperature_c") int temperatureC,
        int humidity,
        @JsonAlias({
                "co2Level", "co2_level" }) int co2Level)
        implements SensorEventDto {
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