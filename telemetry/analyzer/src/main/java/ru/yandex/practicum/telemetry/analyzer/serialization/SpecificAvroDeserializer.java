package ru.yandex.practicum.telemetry.analyzer.serialization;

import org.apache.avro.Schema;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.stereotype.Component;

@Component
public class SpecificAvroDeserializer {

    public <T extends SpecificRecordBase> T deserialize(byte[] payload, Class<T> clazz) {
        try {
            T instance = clazz.getDeclaredConstructor().newInstance();
            Schema schema = instance.getSchema();

            SpecificDatumReader<T> reader = new SpecificDatumReader<>(schema);
            Decoder decoder = DecoderFactory.get().binaryDecoder(payload, null);

            return reader.read(null, decoder);
        } catch (Exception e) {
            throw new IllegalStateException("Avro deserialization failed: " + clazz.getName(), e);
        }
    }
}