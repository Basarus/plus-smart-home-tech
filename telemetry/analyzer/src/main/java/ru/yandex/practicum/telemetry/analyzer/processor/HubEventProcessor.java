package ru.yandex.practicum.telemetry.analyzer.kafka;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.telemetry.analyzer.serialization.AvroDeserializer;
import ru.yandex.practicum.telemetry.analyzer.service.HubEventService;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@RequiredArgsConstructor
public class HubEventProcessor implements Runnable {

    private final org.apache.kafka.clients.consumer.KafkaConsumer<String, byte[]> consumer;
    private final HubEventService hubEventService;
    private final AvroDeserializer avroDeserializer;

    @Value("${app.kafka.topics.hubs}")
    private String hubsTopic;

    @Value("${app.kafka.poll-timeout-ms:500}")
    private long pollTimeoutMs;

    private final AtomicBoolean running = new AtomicBoolean(true);
    private Thread worker;

    @PostConstruct
    public void start() {
        worker = new Thread(this, "hub-event-processor");
        worker.start();
    }

    @PreDestroy
    public void stop() {
        running.set(false);
        consumer.wakeup();
    }

    @Override
    public void run() {
        consumer.subscribe(List.of(hubsTopic));

        try {
            while (running.get()) {
                ConsumerRecords<String, byte[]> records = consumer.poll(Duration.ofMillis(pollTimeoutMs));

                records.forEach(r -> {
                    HubEventAvro event = avroDeserializer.deserialize(r.value(), HubEventAvro.class);
                    hubEventService.handle(event);
                });

                consumer.commitSync();
            }
        } catch (WakeupException e) {
            if (running.get()) throw e;
        } finally {
            consumer.close();
        }
    }
}