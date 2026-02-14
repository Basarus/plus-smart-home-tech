package ru.yandex.practicum.telemetry.collector.api.dto;

import java.time.Instant;

public record SwitchSensorEventDto(
        String id,
        String hubId,
        Instant timestamp,
        boolean state) implements SensorEventDto {
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