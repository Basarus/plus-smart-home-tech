package ru.yandex.practicum.telemetry.collector.kafka;

import jakarta.annotation.PreDestroy;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.telemetry.collector.config.KafkaProps;
import ru.yandex.practicum.telemetry.collector.config.TopicsProperties;

import java.io.ByteArrayOutputStream;
import java.util.Properties;

@Component
public class TelemetryProducer {
    private final TopicsProperties topics;
    private final KafkaProducer<String, byte[]> producer;

    public TelemetryProducer(TopicsProperties topics, KafkaProps props) {
        this.topics = topics;
        this.producer = new KafkaProducer<>(producerProps(props));
    }

    public void sendSensorEvent(SensorEventAvro event) {
        String key = event.getHubId();
        Long timestamp = toKafkaTimestamp(event.getTimestamp());
        producer.send(new ProducerRecord<>(topics.sensors(), null, timestamp, key, serialize(event)));
        producer.flush();
    }

    public void sendHubEvent(HubEventAvro event) {
        String key = event.getHubId();
        Long timestamp = toKafkaTimestamp(event.getTimestamp());
        producer.send(new ProducerRecord<>(topics.hubs(), null, timestamp, key, serialize(event)));
        producer.flush();
    }

    private Properties producerProps(KafkaProps props) {
        Properties p = new Properties();
        p.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, props.bootstrapServers());
        p.put(ProducerConfig.CLIENT_ID_CONFIG, props.clientId());
        p.put(ProducerConfig.ACKS_CONFIG, "all");
        p.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        p.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());
        return p;
    }

    private Long toKafkaTimestamp(Object avroTimestamp) {
        if (avroTimestamp == null) {
            return null;
        }
        if (avroTimestamp instanceof java.time.Instant i) {
            return i.toEpochMilli();
        }
        if (avroTimestamp instanceof Long l) {
            return l;
        }
        if (avroTimestamp instanceof Integer i) {
            return i.longValue();
        }
        throw new IllegalArgumentException("Unsupported timestamp type: " + avroTimestamp.getClass().getName());
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

    @PreDestroy
    public void close() {
        producer.flush();
        producer.close();
    }
}