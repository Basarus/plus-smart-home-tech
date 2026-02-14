package ru.yandex.practicum.telemetry.collector.api.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public record DeviceActionDto(
        @JsonAlias({
                "sensorId", "sensor_id" }) String sensorId,
        String type,
        int value) {
}