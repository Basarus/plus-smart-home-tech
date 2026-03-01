package ru.yandex.practicum.telemetry.analyzer.config;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConsumersConfig {

    @Bean("hubEventsConsumer")
    public KafkaConsumer<String, byte[]> hubEventsConsumer(KafkaClientsFactory factory) {
        return factory.createHubEventsConsumer();
    }

    @Bean("snapshotsConsumer")
    public KafkaConsumer<String, byte[]> snapshotsConsumer(KafkaClientsFactory factory) {
        return factory.createSnapshotsConsumer();
    }
}