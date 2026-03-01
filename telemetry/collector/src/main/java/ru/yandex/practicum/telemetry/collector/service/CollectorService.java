package ru.yandex.practicum.telemetry.collector.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.grpc.telemetry.message.event.HubEventProto;
import ru.yandex.practicum.telemetry.collector.api.dto.HubEventDto;
import ru.yandex.practicum.telemetry.collector.api.dto.SensorEventDto;
import ru.yandex.practicum.telemetry.collector.kafka.TelemetryProducer;
import ru.yandex.practicum.telemetry.collector.mapper.AvroEventMapper;
import ru.yandex.practicum.telemetry.collector.mapper.HubEventProtoMapper;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class CollectorService {

    private final TelemetryProducer producer;
    private final AvroEventMapper mapper;
    private final HubEventProtoMapper hubEventProtoMapper;

    public void collectSensorEvent(SensorEventDto dto) {
        SensorEventAvro avro = mapper.toAvro(dto);
        producer.sendSensorEvent(avro);
    }

    public void collectHubEvent(HubEventDto dto) {
        HubEventProto proto = hubEventProtoMapper.toProto(dto);

        Instant ts = Instant.ofEpochSecond(
                proto.getTimestamp().getSeconds(),
                proto.getTimestamp().getNanos());

        HubEventAvro avro = mapper.toHubEventAvro(proto.getHubId(), ts, proto.toByteArray());
        producer.sendHubEvent(avro);
    }
}