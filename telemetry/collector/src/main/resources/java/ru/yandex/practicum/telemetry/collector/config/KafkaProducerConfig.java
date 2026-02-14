package ru.yandex.practicum.telemetry.collector.config;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import ru.yandex.practicum.telemetry.collector.serialization.SpecificAvroSerializer;

import java.util.HashMap;

@Configuration
public class KafkaProducerConfig {
    @Bean
    public ProducerFactory<String, SpecificRecordBase> producerFactory(KafkaProperties kafkaProperties) {
        var props = new HashMap<>(kafkaProperties.buildProducerProperties(null));
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, SpecificAvroSerializer.class);
        props.putIfAbsent(ProducerConfig.ACKS_CONFIG, "all");
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, SpecificRecordBase> kafkaTemplate(ProducerFactory<String, SpecificRecordBase> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}