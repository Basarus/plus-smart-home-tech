package ru.yandex.practicum.telemetry.collector.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "collector.kafka")
public record KafkaProps(
                @NotBlank String bootstrapServers,
                @NotBlank String clientId) {
        public KafkaProps {
                if (bootstrapServers == null || bootstrapServers.isBlank())
                        bootstrapServers = "localhost:9092";
                if (clientId == null || clientId.isBlank())
                        clientId = "collector";
        }
}