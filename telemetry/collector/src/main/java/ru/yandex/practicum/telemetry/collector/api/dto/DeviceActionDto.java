package ru.yandex.practicum.telemetry.collector.api.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;

public record DeviceActionDto(
                @JsonAlias({
                                "sensorId", "sensor_id" }) @NotBlank String sensorId,

                @NotBlank String type,

                Integer value) {
}