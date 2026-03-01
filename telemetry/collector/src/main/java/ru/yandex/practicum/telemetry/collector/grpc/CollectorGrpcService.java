package ru.yandex.practicum.telemetry.collector.grpc;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.yandex.practicum.grpc.telemetry.collector.CollectorControllerGrpc;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.grpc.telemetry.message.event.HubEventProto;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.telemetry.collector.api.dto.ClimateSensorEventDto;
import ru.yandex.practicum.telemetry.collector.api.dto.LightSensorEventDto;
import ru.yandex.practicum.telemetry.collector.api.dto.MotionSensorEventDto;
import ru.yandex.practicum.telemetry.collector.api.dto.SensorEventDto;
import ru.yandex.practicum.telemetry.collector.api.dto.SwitchSensorEventDto;
import ru.yandex.practicum.telemetry.collector.api.dto.TemperatureSensorEventDto;
import ru.yandex.practicum.telemetry.collector.kafka.TelemetryProducer;
import ru.yandex.practicum.telemetry.collector.mapper.AvroEventMapper;

import java.time.Instant;

@GrpcService
@RequiredArgsConstructor
public class CollectorGrpcService extends CollectorControllerGrpc.CollectorControllerImplBase {

    private final TelemetryProducer producer;
    private final AvroEventMapper mapper;

    @Override
    public void collectSensorEvent(SensorEventProto request, StreamObserver<Empty> responseObserver) {
        try {
            SensorEventDto dto = toSensorDto(request);
            SensorEventAvro avro = mapper.toAvro(dto);
            producer.sendSensorEvent(avro);
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INVALID_ARGUMENT
                            .withDescription("Failed to handle sensor event: " + e.getMessage())
                            .withCause(e)
                            .asRuntimeException());
        }
    }

    @Override
    public void collectHubEvent(HubEventProto request, StreamObserver<Empty> responseObserver) {
        try {
            String hubId = request.getHubId();
            Instant ts = toInstant(request.getTimestamp());
            byte[] payload = request.toByteArray();

            HubEventAvro avro = mapper.toHubEventAvro(request.getHubId(), ts, payload);;

            producer.sendHubEvent(avro);

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INVALID_ARGUMENT
                            .withDescription("Failed to handle hub event: " + e.getMessage())
                            .withCause(e)
                            .asRuntimeException());
        }
    }

    private SensorEventDto toSensorDto(SensorEventProto event) {
        Instant ts = toInstant(event.getTimestamp());
        String id = event.getId();
        String hubId = event.getHubId();

        return switch (event.getPayloadCase()) {
            case MOTION_SENSOR -> {
                var p = event.getMotionSensor();
                yield new MotionSensorEventDto(
                        id,
                        hubId,
                        ts,
                        p.getLinkQuality(),
                        p.getMotion(),
                        p.getVoltage());
            }
            case TEMPERATURE_SENSOR -> {
                var p = event.getTemperatureSensor();
                yield new TemperatureSensorEventDto(
                        id,
                        hubId,
                        ts,
                        p.getTemperatureC(),
                        p.getTemperatureF());
            }
            case LIGHT_SENSOR -> {
                var p = event.getLightSensor();
                yield new LightSensorEventDto(
                        id,
                        hubId,
                        ts,
                        p.getLinkQuality(),
                        p.getLuminosity());
            }
            case CLIMATE_SENSOR -> {
                var p = event.getClimateSensor();
                yield new ClimateSensorEventDto(
                        id,
                        hubId,
                        ts,
                        p.getTemperatureC(),
                        p.getHumidity(),
                        p.getCo2Level());
            }
            case SWITCH_SENSOR -> {
                var p = event.getSwitchSensor();
                yield new SwitchSensorEventDto(
                        id,
                        hubId,
                        ts,
                        p.getState());
            }
            case PAYLOAD_NOT_SET -> throw new IllegalArgumentException("Sensor payload is not set");
        };
    }

    private Instant toInstant(com.google.protobuf.Timestamp ts) {
        return Instant.ofEpochSecond(ts.getSeconds(), ts.getNanos());
    }
}