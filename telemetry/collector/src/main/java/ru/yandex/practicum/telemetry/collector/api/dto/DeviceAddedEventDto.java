package ru.yandex.practicum.telemetry.collector.api.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.google.protobuf.Timestamp;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record DeviceAddedEventDto(

        @NotBlank @JsonAlias({
                "hubId", "hub_id" }) String hubId,

        @NotNull Timestamp timestamp,

        @NotBlank String id,

        @NotBlank @JsonAlias({ "deviceType", "device_type" }) String deviceType

    ) implements HubEventDto {

    @Override
    public String getHubId() {
        return hubId;
    }

    @Override
    public Timestamp getTimestamp() {
        return timestamp;
    }

    public String getId() {
        return id;
    }

    public String getDeviceType() {
        return deviceType;
    }
}