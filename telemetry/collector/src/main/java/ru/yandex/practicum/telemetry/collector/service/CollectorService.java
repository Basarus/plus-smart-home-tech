package ru.yandex.practicum.telemetry.collector.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.grpc.telemetry.message.event.HubEventProto;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.telemetry.collector.kafka.TelemetryProducer;
import ru.yandex.practicum.telemetry.collector.mapper.AvroEventMapper;

@Service
@RequiredArgsConstructor
public class CollectorService {

    private final TelemetryProducer producer;
    private final AvroEventMapper mapper;

    public void collectSensorEvent(SensorEventProto request) {
        SensorEventAvro avro = mapper.toAvro(request);
        producer.sendSensorEvent(avro);
    }

    public void collectHubEvent(HubEventProto request) {
        HubEventAvro avro = mapper.toHubEventAvro(request);
        producer.sendHubEvent(avro);
    }
}