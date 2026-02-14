package ru.yandex.practicum.telemetry.collector.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "collector.kafka.topics")
public record CollectorKafkaProperties(String sensors, String hubs) {
}