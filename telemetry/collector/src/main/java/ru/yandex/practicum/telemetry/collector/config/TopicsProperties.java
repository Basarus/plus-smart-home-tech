package ru.yandex.practicum.telemetry.collector.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.kafka.topics")
public record TopicsProperties(
        String sensors,
        String hubs
) {
}