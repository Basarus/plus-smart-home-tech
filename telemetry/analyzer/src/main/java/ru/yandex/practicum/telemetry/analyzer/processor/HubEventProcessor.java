package ru.yandex.practicum.telemetry.analyzer.processor;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.telemetry.analyzer.config.AnalyzerKafkaProperties;
import ru.yandex.practicum.telemetry.analyzer.service.HubEventService;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class HubEventProcessor implements Runnable {

    private final KafkaConsumer<String, byte[]> consumer;
    private final AnalyzerKafkaProperties props;
    private final HubEventService hubEventService;
    private final AtomicBoolean running = new AtomicBoolean(true);

    public HubEventProcessor(
            KafkaConsumer<String, byte[]> consumer,
            AnalyzerKafkaProperties props,
            HubEventService hubEventService
    ) {
        this.consumer = consumer;
        this.props = props;
        this.hubEventService = hubEventService;
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }

    @Override
    public void run() {
        consumer.subscribe(List.of(props.getTopics().getHubs()));
        try {
            while (running.get()) {
                ConsumerRecords<String, byte[]> records = consumer.poll(Duration.ofMillis(props.getPollTimeoutMs()));
                records.forEach(r -> hubEventService.handle(r.value()));
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