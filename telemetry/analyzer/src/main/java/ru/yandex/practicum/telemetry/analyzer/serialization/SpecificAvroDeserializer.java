package ru.yandex.practicum.telemetry.analyzer.serialization;

import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.stereotype.Component;

@Component
public class SpecificAvroDeserializer {
    public <T extends SpecificRecordBase> T deserialize(byte[] data, Class<T> clazz) {
        try {
            SpecificDatumReader<T> reader = new SpecificDatumReader<>(clazz);
            BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(data, null);
            return reader.read(null, decoder);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}