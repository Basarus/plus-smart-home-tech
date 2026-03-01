package ru.yandex.practicum.telemetry.collector.grpc;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.yandex.practicum.grpc.telemetry.collector.CollectorControllerGrpc;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.grpc.telemetry.message.event.HubEventProto;
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
            SensorEventDto dto = toSensorDto(request);
            SensorEventAvro avro = mapper.toAvro(dto);
            producer.sendSensorEvent(avro);

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
    public void collectHubEvent(HubEventProto request, StreamObserver<Empty> responseObserver) {
        try {
            HubMapping mapping = toHubDtoAndPayloadBytes(request);

            HubEventAvro avro = mapper.toHubEventAvro(
                    mapping.dto().getHubId(),
                    mapping.dto().getTimestamp(),
                    mapping.payloadBytes()
            );

            producer.sendHubEvent(avro);

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
            case MOTION_SENSOR -> {
                var p = event.getMotionSensor();
                yield new MotionSensorEventDto(
                        id,
                        hubId,
                        ts,
                        p.getLinkQuality(),
                        p.getMotion(),
                        p.getVoltage()
                );
            }
            case LIGHT_SENSOR -> {
                var p = event.getLightSensor();
                yield new LightSensorEventDto(
                        id,
                        hubId,
                        ts,
                        p.getLinkQuality(),
                        p.getLuminosity()
                );
            }
            case CLIMATE_SENSOR -> {
                var p = event.getClimateSensor();
                yield new ClimateSensorEventDto(
                        id,
                        hubId,
                        ts,
                        p.getTemperatureC(),
                        p.getHumidity(),
                        p.getCo2Level()
                );
            }
            case SWITCH_SENSOR -> {
                var p = event.getSwitchSensor();
                yield new SwitchSensorEventDto(
                        id,
                        hubId,
                        ts,
                        p.getState()
                );
            }
            case TEMPERATURE_SENSOR -> {
                var p = event.getTemperatureSensor();
                yield new TemperatureSensorEventDto(
                        id,
                        hubId,
                        ts,
                        p.getTemperatureC(),
                        p.getTemperatureF()
                );
            }
            case PAYLOAD_NOT_SET -> throw new IllegalArgumentException("Sensor payload is not set");
        };
    }

    private HubMapping toHubDtoAndPayloadBytes(HubEventProto event) {
        Instant ts = toInstant(event.getTimestamp());
        String hubId = event.getHubId();

        return switch (event.getPayloadCase()) {
            case DEVICE_ADDED -> {
                var p = event.getDeviceAdded();
                HubEventDto dto = new DeviceAddedEventDto(
                        hubId,
                        ts,
                        p.getId(),
                        p.getType().name()
                );
                yield new HubMapping(dto, p.toByteArray());
            }
            case DEVICE_REMOVED -> {
                var p = event.getDeviceRemoved();
                HubEventDto dto = new DeviceRemovedEventDto(
                        hubId,
                        ts,
                        p.getId()
                );
                yield new HubMapping(dto, p.toByteArray());
            }
            case SCENARIO_ADDED -> {
                var p = event.getScenarioAdded();

                List<ScenarioConditionDto> conditions = p.getConditionsList().stream()
                        .map(c -> new ScenarioConditionDto(
                                c.getSensorId(),
                                c.getCondition().getType().name(),
                                c.getCondition().getOperation().name(),
                                c.getCondition().getValue()
                        ))
                        .toList();

                List<DeviceActionDto> actions = p.getActionsList().stream()
                        .map(a -> new DeviceActionDto(
                                a.getSensorId(),
                                a.getAction().getType().name(),
                                a.getAction().getValue()
                        ))
                        .toList();

                HubEventDto dto = new ScenarioAddedEventDto(
                        hubId,
                        ts,
                        p.getName(),
                        conditions,
                        actions
                );
                yield new HubMapping(dto, p.toByteArray());
            }
            case SCENARIO_REMOVED -> {
                var p = event.getScenarioRemoved();
                HubEventDto dto = new ScenarioRemovedEventDto(
                        hubId,
                        ts,
                        p.getName()
                );
                yield new HubMapping(dto, p.toByteArray());
            }
            case DEVICE_ACTION_REQUEST -> throw new IllegalArgumentException("Unsupported hub event payload: DEVICE_ACTION_REQUEST");
            case PAYLOAD_NOT_SET -> throw new IllegalArgumentException("Hub payload is not set");
        };
    }

    private Instant toInstant(com.google.protobuf.Timestamp ts) {
        return Instant.ofEpochSecond(ts.getSeconds(), ts.getNanos());
    }

    private record HubMapping(HubEventDto dto, byte[] payloadBytes) {
    }
}