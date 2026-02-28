package ru.yandex.practicum.telemetry.analyzer.processor;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.telemetry.analyzer.config.AnalyzerKafkaProperties;
import ru.yandex.practicum.telemetry.analyzer.service.ScenarioEngine;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
public class SnapshotProcessor {

    private final KafkaConsumer<String, Object> consumer;
    private final AnalyzerKafkaProperties props;
    private final ScenarioEngine scenarioEngine;
    private final AtomicBoolean running = new AtomicBoolean(true);

    public SnapshotProcessor(
            KafkaConsumer<String, Object> consumer,
            AnalyzerKafkaProperties props,
            ScenarioEngine scenarioEngine
    ) {
        this.consumer = consumer;
        this.props = props;
        this.scenarioEngine = scenarioEngine;
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }

    public void start() {
        consumer.subscribe(List.of(props.getTopics().getSnapshots()));
        try {
            while (running.get()) {
                ConsumerRecords<String, Object> records = consumer.poll(Duration.ofMillis(props.getPollTimeoutMs()));
                if (records.isEmpty()) continue;

                records.forEach(r -> scenarioEngine.handleSnapshot(r.value()));
                consumer.commitSync();
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