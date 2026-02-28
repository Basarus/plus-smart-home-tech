package ru.yandex.practicum.telemetry.collector.api.dto;

import com.google.protobuf.Timestamp;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record SwitchSensorEventDto(

        @NotBlank String id,

        @NotBlank String hubId,

        @NotNull Timestamp timestamp,

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
    public Timestamp getTimestamp() {
        return timestamp;
    }
}