package ru.yandex.practicum.telemetry.analyzer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "analyzer.kafka")
public record AnalyzerKafkaProperties(
        String bootstrapServers,
        Topics topics,
        Consumers consumers,
        Snapshot snapshot
) {
    public record Topics(String hubs, String snapshots) {
    }

    public record Consumers(HubEvents hubEvents, Snapshots snapshots) {
        public record HubEvents(String groupId, int pollTimeoutMs, int maxPollRecords, boolean enableAutoCommit) {
        }

        public record Snapshots(String groupId, int pollTimeoutMs, int maxPollRecords, boolean enableAutoCommit) {
        }
    }

    public record Snapshot(String valueClass) {
    }
}