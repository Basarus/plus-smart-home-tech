package ru.yandex.practicum.telemetry.collector.grpc;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.grpc.telemetry.collector.CollectorControllerGrpc;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.telemetry.collector.api.dto.ClimateSensorEventDto;
import ru.yandex.practicum.telemetry.collector.api.dto.DeviceActionDto;
import ru.yandex.practicum.telemetry.collector.api.dto.DeviceAddedEventDto;
import ru.yandex.practicum.telemetry.collector.api.dto.DeviceRemovedEventDto;
import ru.yandex.practicum.telemetry.collector.api.dto.HubEventDto;
import ru.yandex.practicum.telemetry.collector.api.dto.LightSensorEventDto;
import ru.yandex.practicum.telemetry.collector.api.dto.MotionSensorEventDto;
import ru.yandex.practicum.telemetry.collector.api.dto.ScenarioAddedEventDto;
import ru.yandex.practicum.telemetry.collector.api.dto.ScenarioConditionDto;
import ru.yandex.practicum.telemetry.collector.api.dto.ScenarioRemovedEventDto;
import ru.yandex.practicum.telemetry.collector.api.dto.SensorEventDto;
import ru.yandex.practicum.telemetry.collector.api.dto.SwitchSensorEventDto;
import ru.yandex.practicum.telemetry.collector.api.dto.TemperatureSensorEventDto;
import ru.yandex.practicum.telemetry.collector.kafka.TelemetryProducer;
import ru.yandex.practicum.telemetry.collector.mapper.AvroEventMapper;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CollectorGrpcService extends CollectorControllerGrpc.CollectorControllerImplBase {

    private static final String SENSORS_TOPIC = "telemetry.sensors.v1";
    private static final String HUBS_TOPIC = "telemetry.hubs.v1";

    private final TelemetryProducer producer;
    private final AvroEventMapper mapper;

    @Override
    public void collectSensorEvent(SensorEventProto request, StreamObserver<Empty> responseObserver) {
        try {
            SensorEventDto dto = toSensorDto(request);
            SensorEventAvro avro = mapper.toAvro(dto);

            producer.send(SENSORS_TOPIC, dto.getHubId(), avro);

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("Failed to handle sensor event: " + e.getMessage())
                    .withCause(e)
                    .asRuntimeException());
        }
    }

    @Override
    public void collectHubEvent(
            ru.yandex.practicum.grpc.telemetry.message.event.HubEventProto request,
            StreamObserver<Empty> responseObserver) {
        try {
            HubMapping mapping = toHubDtoAndPayloadBytes(request);

            HubEventAvro avro = mapper.toHubEventAvro(
                    mapping.dto().getHubId(),
                    mapping.dto().getTimestamp(),
                    mapping.payloadBytes());

            producer.send(HUBS_TOPIC, mapping.dto().getHubId(), avro);

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INVALID_ARGUMENT
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
            case MOTION -> {
                var p = event.getMotion();
                yield new MotionSensorEventDto(
                        id, hubId, ts,
                        p.getLinkQuality(),
                        p.getMotion(),
                        p.getVoltage());
            }
            case LIGHT -> {
                var p = event.getLight();
                yield new LightSensorEventDto(
                        id, hubId, ts,
                        p.getLinkQuality(),
                        p.getLuminosity(),
                        p.getVoltage());
            }
            case CLIMATE -> {
                var p = event.getClimate();
                yield new ClimateSensorEventDto(
                        id, hubId, ts,
                        p.getLinkQuality(),
                        p.getTemperatureC(),
                        p.getHumidity(),
                        p.getCo2Level(),
                        p.getVoltage());
            }
            case SWITCH -> {
                var p = event.getSwitch();
                yield new SwitchSensorEventDto(
                        id, hubId, ts,
                        p.getLinkQuality(),
                        p.getState(),
                        p.getVoltage());
            }
            case TEMPERATURE -> {
                var p = event.getTemperature();
                yield new TemperatureSensorEventDto(
                        id, hubId, ts,
                        p.getTemperatureC(),
                        p.getHumidity());
            }
            case PAYLOAD_NOT_SET -> throw new IllegalArgumentException("Sensor payload is not set");
        };
    }

    private HubMapping toHubDtoAndPayloadBytes(ru.yandex.practicum.grpc.telemetry.message.event.HubEventProto event) {
        Instant ts = toInstant(event.getTimestamp());
        String hubId = event.getHubId();

        return switch (event.getPayloadCase()) {
            case DEVICE_ADDED -> {
                var p = event.getDeviceAdded();
                HubEventDto dto = new DeviceAddedEventDto(
                        hubId,
                        ts,
                        p.getId(),
                        p.getDeviceType().name());
                yield new HubMapping(dto, p.toByteArray());
            }
            case DEVICE_REMOVED -> {
                var p = event.getDeviceRemoved();
                HubEventDto dto = new DeviceRemovedEventDto(
                        hubId,
                        ts,
                        p.getId());
                yield new HubMapping(dto, p.toByteArray());
            }
            case SCENARIO_ADDED -> {
                var p = event.getScenarioAdded();

                List<ScenarioConditionDto> conditions = p.getConditionsList().stream()
                        .map(c -> new ScenarioConditionDto(
                                c.getSensorId(),
                                c.getType().name(),
                                c.getOperation().name(),
                                c.getValue()))
                        .toList();

                List<DeviceActionDto> actions = p.getActionsList().stream()
                        .map(a -> new DeviceActionDto(
                                a.getSensorId(),
                                a.getType().name(),
                                a.getValue()))
                        .toList();

                HubEventDto dto = new ScenarioAddedEventDto(
                        hubId,
                        ts,
                        p.getName(),
                        conditions,
                        actions);
                yield new HubMapping(dto, p.toByteArray());
            }
            case SCENARIO_REMOVED -> {
                var p = event.getScenarioRemoved();
                HubEventDto dto = new ScenarioRemovedEventDto(
                        hubId,
                        ts,
                        p.getName());
                yield new HubMapping(dto, p.toByteArray());
            }
            case PAYLOAD_NOT_SET -> throw new IllegalArgumentException("Hub payload is not set");
        };
    }

    private Instant toInstant(com.google.protobuf.Timestamp ts) {
        return Instant.ofEpochSecond(ts.getSeconds(), ts.getNanos());
    }

    private record HubMapping(HubEventDto dto, byte[] payloadBytes) {
    }
}