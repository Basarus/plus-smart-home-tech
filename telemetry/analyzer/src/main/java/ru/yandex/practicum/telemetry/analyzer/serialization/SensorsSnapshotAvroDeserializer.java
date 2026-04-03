package ru.yandex.practicum.telemetry.analyzer.serialization;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

@Component
public class SensorsSnapshotAvroDeserializer {

    private final SpecificAvroDeserializer specificAvroDeserializer;

    public SensorsSnapshotAvroDeserializer(SpecificAvroDeserializer specificAvroDeserializer) {
        this.specificAvroDeserializer = specificAvroDeserializer;
    }

    public SensorsSnapshotAvro deserialize(byte[] payload) {
        return specificAvroDeserializer.deserialize(payload, SensorsSnapshotAvro.class);
    }
}