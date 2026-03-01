package ru.yandex.practicum.telemetry.aggregator.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.telemetry.aggregator.config.AggregatorProperties;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
@RequiredArgsConstructor
public class AggregationStarter {

    private final KafkaConsumer<String, SensorEventAvro> consumer;
    private final KafkaProducer<String, SensorsSnapshotAvro> producer;
    private final AggregatorProperties props;
    private final SnapshotAggregator aggregator;

    private final AtomicBoolean running = new AtomicBoolean(true);

    public void start() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
        consumer.subscribe(List.of(props.getTopics().getSensors()));

        try {
            while (running.get()) {
                ConsumerRecords<String, SensorEventAvro> records =
                        consumer.poll(Duration.ofMillis(props.getKafka().getPollTimeoutMs()));

                if (records.isEmpty()) continue;

                records.forEach(r -> processEvent(r.value()));

                try {
                    producer.flush();
                } catch (Exception e) {
                    log.warn("producer.flush failed", e);
                }

                if (!props.getKafka().isEnableAutoCommit()) {
                    try {
                        consumer.commitSync();
                    } catch (Exception e) {
                        log.warn("commitSync failed", e);
                    }
                }
            }
        } catch (WakeupException e) {
            if (running.get()) throw e;
        } catch (Exception e) {
            log.error("Ошибка во время обработки событий от датчиков", e);
        } finally {
            try {
                try {
                    producer.flush();
                } catch (Exception e) {
                    log.warn("producer.flush failed", e);
                }
                if (!props.getKafka().isEnableAutoCommit()) {
                    try {
                        consumer.commitSync();
                    } catch (Exception e) {
                        log.warn("final commitSync failed", e);
                    }
                }
            } finally {
                try {
                    consumer.close();
                } catch (Exception e) {
                    log.warn("consumer.close failed", e);
                }
                try {
                    producer.close();
                } catch (Exception e) {
                    log.warn("producer.close failed", e);
                }
            }
        }
    }

    private void processEvent(SensorEventAvro event) {
        if (event == null) return;

        Optional<SensorsSnapshotAvro> updated = aggregator.updateState(event);
        if (updated.isEmpty()) return;

        SensorsSnapshotAvro snapshot = updated.get();
        String hubId = snapshot.getHubId() == null ? null : snapshot.getHubId().toString();

        producer.send(new ProducerRecord<>(props.getTopics().getSnapshots(), hubId, snapshot));
    }

    public void shutdown() {
        running.set(false);
        consumer.wakeup();
    }
}