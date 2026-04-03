package ru.yandex.practicum.telemetry.collector.api.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import com.google.protobuf.Timestamp;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
                @JsonSubTypes.Type(value = DeviceAddedEventDto.class, name = "DEVICE_ADDED"),
                @JsonSubTypes.Type(value = DeviceRemovedEventDto.class, name = "DEVICE_REMOVED"),
                @JsonSubTypes.Type(value = ScenarioAddedEventDto.class, name = "SCENARIO_ADDED"),
                @JsonSubTypes.Type(value = ScenarioRemovedEventDto.class, name = "SCENARIO_REMOVED")
})
public sealed interface HubEventDto
                permits DeviceAddedEventDto, DeviceRemovedEventDto, ScenarioAddedEventDto, ScenarioRemovedEventDto {
        @NotBlank
        String getHubId();

        @NotNull
        Timestamp getTimestamp();
}