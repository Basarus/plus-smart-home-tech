package ru.yandex.practicum.telemetry.collector.api.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.google.protobuf.Timestamp;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record ClimateSensorEventDto(

        @NotBlank String id,

        @NotBlank String hubId,

        @NotNull Timestamp timestamp,

        @NotNull @JsonAlias("temperature_c") Integer temperatureC,

        @NotNull Integer humidity,

        @NotNull @JsonAlias({
                "co2Level", "co2_level" }) Integer co2Level

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