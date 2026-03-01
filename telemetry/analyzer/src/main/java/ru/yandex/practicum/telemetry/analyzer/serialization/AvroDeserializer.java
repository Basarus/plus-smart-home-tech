package ru.yandex.practicum.telemetry.analyzer.serialization;

import org.apache.avro.Schema;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificRecord;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@Component
public class AvroDeserializer {

    public <T extends SpecificRecord> T deserialize(byte[] data, Class<T> clazz) {
        try {
            T instance = clazz.getDeclaredConstructor().newInstance();
            Schema schema = instance.getSchema();

            SpecificDatumReader<T> reader = new SpecificDatumReader<>(schema);
            var decoder = DecoderFactory.get().binaryDecoder(new ByteArrayInputStream(data), null);

            return reader.read(null, decoder);
        } catch (IOException e) {
            throw new RuntimeException("Failed to deserialize Avro message", e);
        } catch (Exception e) {
            throw new RuntimeException("Avro reflection error", e);
        }
    }
}