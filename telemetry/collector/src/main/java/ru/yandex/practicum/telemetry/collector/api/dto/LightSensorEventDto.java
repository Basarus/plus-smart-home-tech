package ru.yandex.practicum.telemetry.collector.api.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.time.Instant;

public record LightSensorEventDto(
        String id,
        String hubId,
        Instant timestamp,
        @JsonAlias("link_quality") int linkQuality,
        int luminosity) implements SensorEventDto {
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