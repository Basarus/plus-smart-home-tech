package ru.yandex.practicum.telemetry.collector.grpc;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.yandex.practicum.grpc.telemetry.service.collector.CollectorControllerGrpc;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.MotionSensorEventProto;
import ru.yandex.practicum.grpc.telemetry.event.LightSensorEventProto;
import ru.yandex.practicum.grpc.telemetry.event.ClimateSensorEventProto;
import ru.yandex.practicum.grpc.telemetry.event.SwitchSensorEventProto;
import ru.yandex.practicum.grpc.telemetry.event.TemperatureSensorEventProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceAddedEventProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceRemovedEventProto;
import ru.yandex.practicum.grpc.telemetry.event.ScenarioAddedEventProto;
import ru.yandex.practicum.grpc.telemetry.event.ScenarioRemovedEventProto;
import ru.yandex.practicum.grpc.telemetry.event.ScenarioConditionProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;

import ru.yandex.practicum.telemetry.collector.api.dto.*;
import ru.yandex.practicum.telemetry.collector.service.CollectorService;

import java.time.Instant;
import java.util.List;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class CollectorGrpcService extends CollectorControllerGrpc.CollectorControllerImplBase {

    private final CollectorService collectorService;

    @Override
    public void collectSensorEvent(SensorEventProto request, StreamObserver<Empty> responseObserver) {
        try {
            SensorEventDto dto = mapSensorEvent(request);
            collectorService.collectSensorEvent(dto);

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            log.error("Failed to handle CollectSensorEvent", e);
            responseObserver.onError(Status.INTERNAL.withDescription("Collector error").asRuntimeException());
        }
    }

    @Override
    public void collectHubEvent(HubEventProto request, StreamObserver<Empty> responseObserver) {
        try {
            HubEventDto dto = mapHubEvent(request);
            collectorService.collectHubEvent(dto);

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            log.error("Failed to handle CollectHubEvent", e);
            responseObserver.onError(Status.INTERNAL.withDescription("Collector error").asRuntimeException());
        }
    }

    private static SensorEventDto mapSensorEvent(SensorEventProto e) {
        String sensorId = e.getSensorId();
        String hubId = e.getHubId();
        Instant ts = toInstant(e.getTimestamp());

        return switch (e.getPayloadCase()) {
            case MOTION_SENSOR_EVENT -> {
                MotionSensorEventProto p = e.getMotionSensorEvent();
                yield new MotionSensorEventDto(sensorId, hubId, ts, p.getMotion(), p.getLinkQuality(), p.getVoltage());
            }
            case LIGHT_SENSOR_EVENT -> {
                LightSensorEventProto p = e.getLightSensorEvent();
                yield new LightSensorEventDto(sensorId, hubId, ts, p.getLuminosity(), p.getLinkQuality(), p.getVoltage());
            }
            case CLIMATE_SENSOR_EVENT -> {
                ClimateSensorEventProto p = e.getClimateSensorEvent();
                yield new ClimateSensorEventDto(
                        sensorId, hubId, ts,
                        p.getTemperatureC(), p.getHumidity(), p.getCo2Level(),
                        p.getLinkQuality(), p.getVoltage()
                );
            }
            case SWITCH_SENSOR_EVENT -> {
                SwitchSensorEventProto p = e.getSwitchSensorEvent();
                yield new SwitchSensorEventDto(sensorId, hubId, ts, p.getState(), p.getLinkQuality(), p.getVoltage());
            }
            case TEMPERATURE_SENSOR_EVENT -> {
                TemperatureSensorEventProto p = e.getTemperatureSensorEvent();
                yield new TemperatureSensorEventDto(sensorId, hubId, ts, p.getTemperatureC(), p.getLinkQuality(), p.getVoltage());
            }
            case PAYLOAD_NOT_SET -> throw new IllegalArgumentException("SensorEvent payload is not set");
        };
    }

    private static HubEventDto mapHubEvent(HubEventProto e) {
        String hubId = e.getHubId();
        Instant ts = toInstant(e.getTimestamp());

        return switch (e.getPayloadCase()) {
            case DEVICE_ADDED -> {
                DeviceAddedEventProto p = e.getDeviceAdded();
                yield new DeviceAddedEventDto(hubId, ts, p.getId(), p.getDeviceType().name());
            }
            case DEVICE_REMOVED -> {
                DeviceRemovedEventProto p = e.getDeviceRemoved();
                yield new DeviceRemovedEventDto(hubId, ts, p.getId());
            }
            case SCENARIO_ADDED -> {
                ScenarioAddedEventProto p = e.getScenarioAdded();

                List<ScenarioConditionDto> conditions = p.getConditionsList().stream()
                        .map(CollectorGrpcService::mapCondition)
                        .toList();

                List<DeviceActionDto> actions = p.getActionsList().stream()
                        .map(CollectorGrpcService::mapAction)
                        .toList();

                yield new ScenarioAddedEventDto(hubId, ts, p.getName(), conditions, actions);
            }
            case SCENARIO_REMOVED -> {
                ScenarioRemovedEventProto p = e.getScenarioRemoved();
                yield new ScenarioRemovedEventDto(hubId, ts, p.getName());
            }

            case PAYLOAD_NOT_SET -> throw new IllegalArgumentException("HubEvent payload is not set");
            default -> throw new IllegalArgumentException("Unsupported HubEvent payload: " + e.getPayloadCase());
        };
    }

    private static ScenarioConditionDto mapCondition(ScenarioConditionProto c) {
        return new ScenarioConditionDto(
                c.getSensorId(),
                c.getType().name(),
                c.getOperation().name(),
                c.getValue()
        );
    }

    private static DeviceActionDto mapAction(DeviceActionProto a) {
        return new DeviceActionDto(
                a.getSensorId(),
                a.getType().name(),
                a.getValue()
        );
    }

    private static Instant toInstant(com.google.protobuf.Timestamp ts) {
        return Instant.ofEpochSecond(ts.getSeconds(), ts.getNanos());
    }
}