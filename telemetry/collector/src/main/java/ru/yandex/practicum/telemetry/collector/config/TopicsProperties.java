package ru.yandex.practicum.telemetry.collector.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "collector.topics")
public record TopicsProperties(
                @NotBlank String sensors,
                @NotBlank String hubs) {
        public TopicsProperties {
                if (sensors == null || sensors.isBlank())
                        sensors = "telemetry.sensors.v1";
                if (hubs == null || hubs.isBlank())
                        hubs = "telemetry.hubs.v1";
        }
}