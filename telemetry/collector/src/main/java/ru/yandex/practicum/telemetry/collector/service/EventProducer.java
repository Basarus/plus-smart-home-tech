package ru.yandex.practicum.telemetry.collector.service;

import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.telemetry.collector.config.CollectorKafkaProperties;

@Service
public class EventProducer {
    private final KafkaTemplate<String, SpecificRecordBase> kafkaTemplate;
    private final CollectorKafkaProperties topics;

    public EventProducer(KafkaTemplate<String, SpecificRecordBase> kafkaTemplate, CollectorKafkaProperties topics) {
        this.kafkaTemplate = kafkaTemplate;
        this.topics = topics;
    }

    public void sendSensor(String hubId, SpecificRecordBase event) {
        kafkaTemplate.send(topics.sensors(), hubId, event);
    }

    public void sendHub(String hubId, SpecificRecordBase event) {
        kafkaTemplate.send(topics.hubs(), hubId, event);
    }
}