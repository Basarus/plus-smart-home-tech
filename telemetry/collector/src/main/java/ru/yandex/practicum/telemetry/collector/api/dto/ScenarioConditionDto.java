package ru.yandex.practicum.telemetry.collector.api.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ScenarioConditionDto(

                @NotBlank @JsonAlias({
                                "sensorId", "sensor_id" }) String sensorId,

                @NotBlank String type,

                @NotBlank String operation,

                @NotNull Integer value

        ) {
}