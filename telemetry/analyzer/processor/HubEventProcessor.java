package ru.yandex.practicum.telemetry.analyzer.processor;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.telemetry.analyzer.config.AnalyzerKafkaProperties;
import ru.yandex.practicum.telemetry.analyzer.config.KafkaClientsFactory;
import ru.yandex.practicum.telemetry.analyzer.serialization.SpecificAvroDeserializer;
import ru.yandex.practicum.telemetry.analyzer.service.HubEventService;

import java.time.Duration;
import java.util.Collections;

@Component
public class HubEventProcessor implements Runnable {
    private final KafkaConsumer<String, byte[]> consumer;
    private final AnalyzerKafkaProperties props;
    private final SpecificAvroDeserializer deserializer = new SpecificAvroDeserializer();
    private final HubEventService hubEventService;

    public HubEventProcessor(KafkaClientsFactory factory,
                             AnalyzerKafkaProperties props,
                             HubEventService hubEventService) {
        this.consumer = factory.createHubEventsConsumer();
        this.props = props;
        this.hubEventService = hubEventService;

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                consumer.wakeup();
                consumer.close();
            } catch (Exception ignored) {
            }
        }));
    }

    @Override
    public void run() {
        consumer.subscribe(Collections.singletonList(props.topics().hubs()));
        while (true) {
            ConsumerRecords<String, byte[]> records = consumer.poll(Duration.ofMillis(props.consumers().hubEvents().pollTimeoutMs()));
            records.forEach(r -> {
                HubEventAvro event = deserializer.deserialize(r.value(), HubEventAvro.class);
                hubEventService.handle(event);
            });
        }
    }
}