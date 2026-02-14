package ru.yandex.practicum.telemetry.collector.service;

import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.telemetry.collector.config.TopicsProperties;

@Component
public class EventProducer {
    private final KafkaTemplate<String, SpecificRecordBase> kafkaTemplate;
    private final TopicsProperties topics;

    public EventProducer(KafkaTemplate<String, SpecificRecordBase> kafkaTemplate, TopicsProperties topics) {
        this.kafkaTemplate = kafkaTemplate;
        this.topics = topics;
    }

    public void sendSensorEvent(SensorEventAvro event) {
        kafkaTemplate.send(topics.sensors(), event.getHubId(), event);
    }

    public void sendHubEvent(HubEventAvro event) {
        kafkaTemplate.send(topics.hubs(), event.getHubId(), event);
    }
}