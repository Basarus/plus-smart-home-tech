package ru.yandex.practicum.telemetry.collector.api.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.time.Instant;

public record DeviceAddedEventDto(
        @JsonAlias({
                "hubId", "hub_id" }) String hubId,
        Instant timestamp,
        String id,
        @JsonAlias({ "deviceType", "device_type" }) String deviceType) implements HubEventDto {
    @Override
    public String getHubId() {
        return hubId;
    }

    @Override
    public Instant getTimestamp() {
        return timestamp;
    }

    public String getId() {
        return id;
    }

    public String getDeviceType() {
        return deviceType;
    }
}