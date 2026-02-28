package ru.yandex.practicum.telemetry.analyzer.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class KafkaConsumersConfig {

    @Bean
    @Qualifier("hubEventConsumerProps")
    public Properties hubEventConsumerProps(
            @Value("${analyzer.kafka.bootstrap-servers}") String bootstrapServers,
            @Value("${analyzer.kafka.consumer.hub-events.group-id}") String groupId,
            @Value("${analyzer.kafka.consumer.auto-offset-reset:earliest}") String autoOffsetReset
    ) {
        return consumerProps(bootstrapServers, groupId, autoOffsetReset, "consumer-analyzer-hub-events");
    }

    @Bean
    @Qualifier("snapshotConsumerProps")
    public Properties snapshotConsumerProps(
            @Value("${analyzer.kafka.bootstrap-servers}") String bootstrapServers,
            @Value("${analyzer.kafka.consumer.snapshots.group-id}") String groupId,
            @Value("${analyzer.kafka.consumer.auto-offset-reset:earliest}") String autoOffsetReset
    ) {
        return consumerProps(bootstrapServers, groupId, autoOffsetReset, "consumer-analyzer-snapshots");
    }

    private Properties consumerProps(String bootstrapServers, String groupId, String autoOffsetReset, String clientIdPrefix) {
        Properties p = new Properties();
        p.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        p.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);

        p.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        p.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());

        p.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        p.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        p.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "500");

        p.put(ConsumerConfig.CLIENT_ID_CONFIG, clientIdPrefix + "-" + groupId);

        return p;
    }
}