package ru.yandex.practicum.telemetry.collector.service;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.telemetry.collector.api.dto.HubEventDto;
import ru.yandex.practicum.telemetry.collector.api.dto.SensorEventDto;
import ru.yandex.practicum.telemetry.collector.config.TopicsProperties;
import ru.yandex.practicum.telemetry.collector.kafka.KafkaProducerService;
import ru.yandex.practicum.telemetry.collector.mapper.AvroEventMapper;
import ru.yandex.practicum.grpc.telemetry.message.event.HubEventMessagesProto;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class CollectorService {

    private final KafkaProducerService producerService;
    private final TopicsProperties topics;
    private final AvroEventMapper mapper;
    private final HubEventProtoMapper hubEventProtoMapper;

    public void collectSensorEvent(SensorEventDto dto) {
        SensorEventAvro avro = mapper.toAvro(dto);
        producerService.send(new ProducerRecord<>(topics.getSensors(), avro.getHubId().toString(), avro));
    }

    public void collectHubEvent(HubEventDto dto) {
        HubEventMessagesProto.HubEventProto proto = hubEventProtoMapper.toProto(dto);
        byte[] payload = proto.toByteArray();

        Instant ts = Instant.ofEpochSecond(
                proto.getTimestamp().getSeconds(),
                proto.getTimestamp().getNanos()
        );

        HubEventAvro avro = mapper.toHubEventAvro(proto.getHubId(), ts, payload);
        producerService.send(new ProducerRecord<>(topics.getHubs(), proto.getHubId(), avro));
    }
}