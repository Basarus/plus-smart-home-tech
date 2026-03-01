package ru.yandex.practicum.telemetry.aggregator.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aggregator")
public class AggregatorProperties {

    private final Kafka kafka = new Kafka();
    private final Topics topics = new Topics();

    public Kafka getKafka() {
        return kafka;
    }

    public Topics getTopics() {
        return topics;
    }

    public static class Kafka {
        private String bootstrapServers;
        private String groupId;
        private int pollTimeoutMs;
        private boolean enableAutoCommit;
        private String autoOffsetReset;
        private int maxPollRecords;

        public String getBootstrapServers() {
            return bootstrapServers;
        }

        public void setBootstrapServers(String bootstrapServers) {
            this.bootstrapServers = bootstrapServers;
        }

        public String getGroupId() {
            return groupId;
        }

        public void setGroupId(String groupId) {
            this.groupId = groupId;
        }

        public int getPollTimeoutMs() {
            return pollTimeoutMs;
        }

        public void setPollTimeoutMs(int pollTimeoutMs) {
            this.pollTimeoutMs = pollTimeoutMs;
        }

        public boolean isEnableAutoCommit() {
            return enableAutoCommit;
        }

        public void setEnableAutoCommit(boolean enableAutoCommit) {
            this.enableAutoCommit = enableAutoCommit;
        }

        public String getAutoOffsetReset() {
            return autoOffsetReset;
        }

        public void setAutoOffsetReset(String autoOffsetReset) {
            this.autoOffsetReset = autoOffsetReset;
        }

        public int getMaxPollRecords() {
            return maxPollRecords;
        }

        public void setMaxPollRecords(int maxPollRecords) {
            this.maxPollRecords = maxPollRecords;
        }
    }

    public static class Topics {
        private String sensors;
        private String snapshots;

        public String getSensors() {
            return sensors;
        }

        public void setSensors(String sensors) {
            this.sensors = sensors;
        }

        public String getSnapshots() {
            return snapshots;
        }

        public void setSnapshots(String snapshots) {
            this.snapshots = snapshots;
        }
    }
}