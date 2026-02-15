package ru.yandex.practicum.telemetry.collector.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.kafka")
public record KafkaProps(
        String bootstrapServers,
        String clientId) {
}