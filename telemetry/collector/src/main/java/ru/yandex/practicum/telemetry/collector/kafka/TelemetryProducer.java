package ru.yandex.practicum.telemetry.collector.kafka;

import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.telemetry.collector.config.TopicsProperties;

import java.io.ByteArrayOutputStream;

@Component
public class TelemetryProducer {
    private final KafkaTemplate<String, byte[]> kafkaTemplate;
    private final TopicsProperties topics;

    public TelemetryProducer(KafkaTemplate<String, byte[]> kafkaTemplate, TopicsProperties topics) {
        this.kafkaTemplate = kafkaTemplate;
        this.topics = topics;
    }

    public void sendSensorEvent(SensorEventAvro event) {
        kafkaTemplate.send(topics.sensors(), event.getHubId(), serialize(event));
    }

    public void sendHubEvent(HubEventAvro event) {
        kafkaTemplate.send(topics.hubs(), event.getHubId(), serialize(event));
    }

    private byte[] serialize(SensorEventAvro event) {
        return serializeInternal(event, new SpecificDatumWriter<>(SensorEventAvro.class));
    }

    private byte[] serialize(HubEventAvro event) {
        return serializeInternal(event, new SpecificDatumWriter<>(HubEventAvro.class));
    }

    private <T> byte[] serializeInternal(T event, SpecificDatumWriter<T> writer) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(out, null);
            writer.write(event, encoder);
            encoder.flush();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}