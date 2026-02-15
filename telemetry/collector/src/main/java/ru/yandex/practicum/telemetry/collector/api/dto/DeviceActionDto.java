package ru.yandex.practicum.telemetry.collector.api.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DeviceActionDto(

                @NotBlank @JsonAlias({
                                "sensorId", "sensor_id" }) String sensorId,

                @NotBlank String type,

                @NotNull Integer value

        ) {
}