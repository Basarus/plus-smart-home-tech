package ru.yandex.practicum.telemetry.collector.api.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public record ScenarioConditionDto(
        @JsonAlias({
                "sensorId", "sensor_id" }) String sensorId,
        String type,
        String operation,
        int value) {
}