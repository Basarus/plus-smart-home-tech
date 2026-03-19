package ru.yandex.practicum.telemetry.collector.api.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import com.google.protobuf.Timestamp;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
                @JsonSubTypes.Type(value = MotionSensorEventDto.class, name = "MOTION_SENSOR_EVENT"),
                @JsonSubTypes.Type(value = TemperatureSensorEventDto.class, name = "TEMPERATURE_SENSOR_EVENT"),
                @JsonSubTypes.Type(value = LightSensorEventDto.class, name = "LIGHT_SENSOR_EVENT"),
                @JsonSubTypes.Type(value = ClimateSensorEventDto.class, name = "CLIMATE_SENSOR_EVENT"),
                @JsonSubTypes.Type(value = SwitchSensorEventDto.class, name = "SWITCH_SENSOR_EVENT")
})
public sealed interface SensorEventDto
                permits MotionSensorEventDto, TemperatureSensorEventDto, LightSensorEventDto, ClimateSensorEventDto,
                SwitchSensorEventDto {

        @NotBlank
        String getId();

        @NotBlank
        String getHubId();

        @NotNull
        Timestamp getTimestamp();
}