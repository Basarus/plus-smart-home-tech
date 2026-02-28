package ru.yandex.practicum.telemetry.analyzer.processor;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.telemetry.analyzer.config.AnalyzerKafkaProperties;
import ru.yandex.practicum.telemetry.analyzer.config.KafkaClientsFactory;
import ru.yandex.practicum.telemetry.analyzer.serialization.SpecificAvroDeserializer;
import ru.yandex.practicum.telemetry.analyzer.service.ScenarioEngine;

import java.time.Duration;
import java.util.Collections;

@Component
public class SnapshotProcessor {
    private final KafkaConsumer<String, byte[]> consumer;
    private final AnalyzerKafkaProperties props;
    private final SpecificAvroDeserializer deserializer = new SpecificAvroDeserializer();
    private final ScenarioEngine scenarioEngine;

    public SnapshotProcessor(KafkaClientsFactory factory,
                             AnalyzerKafkaProperties props,
                             ScenarioEngine scenarioEngine) {
        this.consumer = factory.createSnapshotsConsumer();
        this.props = props;
        this.scenarioEngine = scenarioEngine;

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                consumer.wakeup();
                consumer.close();
            } catch (Exception ignored) {
            }
        }));
    }

    public void start() {
        consumer.subscribe(Collections.singletonList(props.topics().snapshots()));
        Class<? extends SpecificRecordBase> snapshotClass = resolveSnapshotClass(props.snapshot().valueClass());

        while (true) {
            ConsumerRecords<String, byte[]> records = consumer.poll(Duration.ofMillis(props.consumers().snapshots().pollTimeoutMs()));
            records.forEach(r -> {
                SpecificRecordBase snapshot = deserializer.deserialize(r.value(), snapshotClass);
                scenarioEngine.handleSnapshot(snapshot);
            });
            consumer.commitSync();
        }
    }

    @SuppressWarnings("unchecked")
    private Class<? extends SpecificRecordBase> resolveSnapshotClass(String fqcn) {
        try {
            Class<?> c = Class.forName(fqcn);
            if (!SpecificRecordBase.class.isAssignableFrom(c)) {
                throw new IllegalArgumentException("snapshot value-class must extend SpecificRecordBase");
            }
            return (Class<? extends SpecificRecordBase>) c;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}