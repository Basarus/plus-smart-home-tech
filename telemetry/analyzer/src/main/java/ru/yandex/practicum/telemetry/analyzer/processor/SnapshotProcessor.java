package ru.yandex.practicum.telemetry.analyzer.processor;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.telemetry.analyzer.config.AnalyzerKafkaProperties;
import ru.yandex.practicum.telemetry.analyzer.service.ScenarioEngine;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
public class SnapshotProcessor {

    private final KafkaConsumer<String, byte[]> consumer;
    private final AnalyzerKafkaProperties props;
    private final ScenarioEngine scenarioEngine;
    private final AtomicBoolean running = new AtomicBoolean(true);

    public SnapshotProcessor(@Qualifier("snapshotsConsumer") KafkaConsumer<String, byte[]> consumer, AnalyzerKafkaProperties props, ScenarioEngine scenarioEngine) {
        this.consumer = consumer;
        this.props = props;
        this.scenarioEngine = scenarioEngine;
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }

    public void start() {
        consumer.subscribe(List.of(props.topics().snapshots()));
        try {
            while (running.get()) {
                ConsumerRecords<String, byte[]> records = consumer.poll(Duration.ofMillis(props.consumers().snapshots().pollTimeoutMs()));
                if (records.isEmpty()) continue;

                try {
                    records.forEach(r -> scenarioEngine.handleSnapshot(r.value()));
                    if (!props.consumers().snapshots().enableAutoCommit()) {
                        consumer.commitSync();
                    }
                } catch (Exception e) {
                    log.error("Failed to process snapshots batch", e);
                }
            }
        } catch (WakeupException e) {
            if (running.get()) throw e;
        } finally {
            try {
                consumer.close();
            } catch (Exception ignored) {
            }
        }
    }

    public void shutdown() {
        running.set(false);
        consumer.wakeup();
    }
}