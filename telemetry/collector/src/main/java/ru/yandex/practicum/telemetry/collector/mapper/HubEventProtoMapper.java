package ru.yandex.practicum.telemetry.collector.mapper;

import java.nio.ByteBuffer;
import java.time.Instant;

import org.springframework.stereotype.Component;

import ru.yandex.practicum.grpc.telemetry.message.event.HubEventProto;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

@Component
public class HubEventProtoMapper {

    public HubEventAvro toHubEventAvro(HubEventProto proto) {
        HubEventAvro avro = new HubEventAvro();
        avro.setHubId(proto.getHubId());

        Instant ts = Instant.ofEpochSecond(
                proto.getTimestamp().getSeconds(),
                proto.getTimestamp().getNanos()
        );
        avro.setTimestamp(ts);

        avro.setPayload(ByteBuffer.wrap(proto.toByteArray()));
        return avro;
    }
}