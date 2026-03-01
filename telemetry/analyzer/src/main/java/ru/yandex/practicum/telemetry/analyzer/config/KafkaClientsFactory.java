package ru.yandex.practicum.telemetry.analyzer.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
public class KafkaClientsFactory {
    private final AnalyzerKafkaProperties props;

    public KafkaClientsFactory(AnalyzerKafkaProperties props) {
        this.props = props;
    }

    public KafkaConsumer<String, byte[]> createHubEventsConsumer() {
        Properties p = new Properties();
        p.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, props.bootstrapServers());
        p.put(ConsumerConfig.GROUP_ID_CONFIG, props.consumers().hubEvents().groupId());
        p.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        p.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());
        p.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, String.valueOf(props.consumers().hubEvents().enableAutoCommit()));
        p.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, String.valueOf(props.consumers().hubEvents().maxPollRecords()));
        p.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new KafkaConsumer<>(p);
    }

    public KafkaConsumer<String, byte[]> createSnapshotsConsumer() {
        Properties p = new Properties();
        p.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, props.bootstrapServers());
        p.put(ConsumerConfig.GROUP_ID_CONFIG, props.consumers().snapshots().groupId());
        p.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        p.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());
        p.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, String.valueOf(props.consumers().snapshots().enableAutoCommit()));
        p.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, String.valueOf(props.consumers().snapshots().maxPollRecords()));
        p.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new KafkaConsumer<>(p);
    }
}