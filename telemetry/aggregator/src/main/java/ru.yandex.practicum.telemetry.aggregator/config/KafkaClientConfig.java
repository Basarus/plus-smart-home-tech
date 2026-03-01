package ru.yandex.practicum.telemetry.aggregator.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.telemetry.aggregator.avro.SensorEventDeserializer;
import ru.yandex.practicum.telemetry.aggregator.kafka.AvroSerializer;

import java.util.Properties;

@Configuration
public class KafkaClientConfig {

    @Bean
    public KafkaConsumer<String, ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro> sensorEventConsumer(AggregatorProperties props) {
        Properties p = new Properties();
        p.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, props.getKafka().getBootstrapServers());
        p.put(ConsumerConfig.GROUP_ID_CONFIG, props.getKafka().getGroupId());
        p.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        p.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, SensorEventDeserializer.class.getName());
        p.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, String.valueOf(props.getKafka().isEnableAutoCommit()));
        p.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, props.getKafka().getAutoOffsetReset());
        p.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, String.valueOf(props.getKafka().getMaxPollRecords()));
        return new KafkaConsumer<>(p);
    }

    @Bean
    public KafkaProducer<String, SensorsSnapshotAvro> snapshotProducer(AggregatorProperties props) {
        Properties p = new Properties();
        p.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, props.getKafka().getBootstrapServers());
        p.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        p.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, AvroSerializer.class.getName());
        p.put(ProducerConfig.ACKS_CONFIG, "all");
        return new KafkaProducer<>(p);
    }
}