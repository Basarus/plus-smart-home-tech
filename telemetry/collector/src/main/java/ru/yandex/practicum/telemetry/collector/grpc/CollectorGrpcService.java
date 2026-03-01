package ru.yandex.practicum.telemetry.collector.grpc;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.yandex.practicum.grpc.telemetry.collector.CollectorControllerGrpc;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.grpc.telemetry.message.event.HubEventMessagesProto;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.telemetry.collector.kafka.TelemetryProducer;
import ru.yandex.practicum.telemetry.collector.mapper.AvroEventMapper;

import java.time.Instant;

@GrpcService
public class CollectorGrpcService extends CollectorControllerGrpc.CollectorControllerImplBase {

    private final TelemetryProducer producer;
    private final AvroEventMapper mapper;

    public CollectorGrpcService(TelemetryProducer producer, AvroEventMapper mapper) {
        this.producer = producer;
        this.mapper = mapper;
    }

    @Override
    public void collectSensorEvent(SensorEventProto request, StreamObserver<Empty> responseObserver) {
        try {
            SensorEventAvro avro = mapper.toSensorEventAvro(request);
            producer.sendSensorEvent(avro);

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Failed to process sensor event: " + e.getMessage())
                            .withCause(e)
                            .asRuntimeException()
            );
        }
    }

    @Override
    public void collectHubEvent(HubEventMessagesProto.HubEventProto request, StreamObserver<Empty> responseObserver) {
        try {
            byte[] payloadBytes = extractHubPayloadBytes(request);

            HubEventAvro avro = HubEventAvro.newBuilder()
                    .setHubId(request.getHubId())
                    .setTimestamp(Instant.ofEpochSecond(request.getTimestamp().getSeconds(), request.getTimestamp().getNanos()))
                    .setPayload(payloadBytes)
                    .build();

            producer.sendHubEvent(avro);

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Failed to process hub event: " + e.getMessage())
                            .withCause(e)
                            .asRuntimeException()
            );
        }
    }

    private byte[] extractHubPayloadBytes(HubEventMessagesProto.HubEventProto request) {
        return switch (request.getPayloadCase()) {
            case DEVICE_ADDED -> request.getDeviceAdded().toByteArray();
            case DEVICE_REMOVED -> request.getDeviceRemoved().toByteArray();
            case SCENARIO_ADDED -> request.getScenarioAdded().toByteArray();
            case SCENARIO_REMOVED -> request.getScenarioRemoved().toByteArray();
            case PAYLOAD_NOT_SET -> throw new IllegalArgumentException("HubEventProto payload is not set");
        };
    }
}