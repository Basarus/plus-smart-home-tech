package ru.yandex.practicum.telemetry.analyzer.processor;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.telemetry.analyzer.serialization.SensorsSnapshotAvroDeserializer;
import ru.yandex.practicum.telemetry.analyzer.service.ScenarioEngine;

import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Component
public class SnapshotProcessor implements Runnable {

    private final Properties consumerProps;
    private final String topic;
    private final ScenarioEngine scenarioEngine;
    private final SensorsSnapshotAvroDeserializer snapshotDeserializer;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean started = new AtomicBoolean(false);
    private final AtomicReference<KafkaConsumer<String, byte[]>> consumerRef = new AtomicReference<>();
    private volatile Thread worker;

    public SnapshotProcessor(@Qualifier("snapshotConsumerProps") Properties consumerProps, @Value("${analyzer.kafka.topics.snapshots:telemetry.snapshots.v1}") String topic, ScenarioEngine scenarioEngine, SensorsSnapshotAvroDeserializer snapshotDeserializer) {
        this.consumerProps = consumerProps;
        this.topic = topic;
        this.scenarioEngine = scenarioEngine;
        this.snapshotDeserializer = snapshotDeserializer;
    }

    @PostConstruct
    public void start() {
        if (!started.compareAndSet(false, true)) {
            return;
        }

        running.set(true);

        worker = new Thread(this, "snapshot-processor");
        worker.setDaemon(true);
        worker.start();
    }

    @PreDestroy
    public void stop() {
        running.set(false);

        KafkaConsumer<String, byte[]> c = consumerRef.get();
        if (c != null) {
            c.wakeup();
        }

        Thread t = worker;
        if (t != null) {
            try {
                t.join(2_000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public void run() {
        KafkaConsumer<String, byte[]> consumer = new KafkaConsumer<>(consumerProps);
        consumerRef.set(consumer);

        boolean processedSomething = false;

        try {
            consumer.subscribe(List.of(topic));

            while (running.get()) {
                ConsumerRecords<String, byte[]> records = consumer.poll(Duration.ofMillis(500));
                if (records.isEmpty()) {
                    continue;
                }

                for (var record : records) {
                    SensorsSnapshotAvro snapshot = snapshotDeserializer.deserialize(record.value());
                    scenarioEngine.handleSnapshot(snapshot);
                }

                processedSomething = true;

                consumer.commitAsync((offsets, exception) -> {
                    if (exception != null) {
                        log.warn("Async commit failed: {}", offsets, exception);
                    }
                });
            }
        } catch (WakeupException ignored) {
        } catch (Exception e) {
            log.error("SnapshotProcessor failed", e);
        } finally {
            try {
                if (processedSomething) {
                    try {
                        consumer.commitSync();
                    } catch (Exception e) {
                        log.warn("Final commitSync failed", e);
                    }
                }
                consumer.close();
            } catch (Exception ignored) {
            } finally {
                consumerRef.set(null);
            }
        }
    }
}