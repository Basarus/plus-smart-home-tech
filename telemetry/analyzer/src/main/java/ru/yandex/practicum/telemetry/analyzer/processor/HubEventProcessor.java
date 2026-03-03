package ru.yandex.practicum.telemetry.analyzer.processor;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.telemetry.analyzer.service.HubEventService;

import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class HubEventProcessor implements Runnable {

    private final Properties consumerProps;
    private final HubEventService hubEventService;

    @Value("${app.kafka.topics.hubs:telemetry.hubs.v1}")
    private String topic;

    private final AtomicBoolean running = new AtomicBoolean(true);
    private Thread worker;

    private volatile KafkaConsumer<String, byte[]> consumer;

    public HubEventProcessor(
            @Qualifier("hubEventConsumerProps") Properties consumerProps,
            HubEventService hubEventService
    ) {
        this.consumerProps = consumerProps;
        this.hubEventService = hubEventService;
    }

    @PostConstruct
    public void start() {
        worker = new Thread(this, "hub-event-processor");
        worker.start();
    }

    @PreDestroy
    public void stop() {
        running.set(false);
        KafkaConsumer<String, byte[]> c = consumer;
        if (c != null) c.wakeup();
    }

    @Override
    public void run() {
        consumer = new KafkaConsumer<>(consumerProps);
        consumer.subscribe(List.of(topic));

        try {
            while (running.get()) {
                ConsumerRecords<String, byte[]> records = consumer.poll(Duration.ofMillis(500));
                if (records.isEmpty()) {
                    continue;
                }

                for (var r : records) {
                    hubEventService.handleHubEvent(r.value());
                }

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
}