package ru.yandex.practicum.telemetry.collector.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.telemetry.collector.api.dto.HubEventDto;
import ru.yandex.practicum.telemetry.collector.api.dto.SensorEventDto;
import ru.yandex.practicum.telemetry.collector.kafka.TelemetryProducer;
import ru.yandex.practicum.telemetry.collector.mapper.AvroEventMapper;

@Service
public class CollectorService {
    private final TelemetryProducer producer;
    private final AvroEventMapper mapper;

    public CollectorService(TelemetryProducer producer, AvroEventMapper mapper) {
        this.producer = producer;
        this.mapper = mapper;
    }

    public void collectSensorEvent(SensorEventDto dto) {
        producer.sendSensorEvent(mapper.toAvro(dto));
    }

    public void collectHubEvent(HubEventDto dto) {
        producer.sendHubEvent(mapper.toAvro(dto));
    }
}