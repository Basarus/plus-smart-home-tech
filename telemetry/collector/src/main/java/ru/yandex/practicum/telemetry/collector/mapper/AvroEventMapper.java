package ru.yandex.practicum.telemetry.collector.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.telemetry.collector.api.dto.*;

import java.nio.ByteBuffer;
import java.time.Instant;

@Component
public class AvroEventMapper {

    public SensorEventAvro toAvro(SensorEventDto dto) {
        Object payload = switch (dto) {
            case MotionSensorEventDto e -> motion(e);
            case TemperatureSensorEventDto e -> temperature(e);
            case LightSensorEventDto e -> light(e);
            case ClimateSensorEventDto e -> climate(e);
            case SwitchSensorEventDto e -> sw(e);
            default -> throw new IllegalArgumentException("Unsupported sensor event: " + dto.getClass().getName());
        };

        SensorEventAvro avro = new SensorEventAvro();
        avro.setId(dto.getId());
        avro.setHubId(dto.getHubId());
        avro.setTimestamp(dto.getTimestamp());
        avro.setPayload(payload);
        return avro;
    }

    public static HubEventAvro toHubEventAvro(HubEventProto proto) {
        var builder = HubEventAvro.newBuilder()
                .setHubId(proto.getHubId())
                .setTimestamp(Instant.ofEpochMilli(proto.getTimestamp()));

        Object payload;

        switch (proto.getPayloadCase()) {
            case DEVICE_ADDED -> {
                var p = proto.getDeviceAdded();
                payload = DeviceAddedEventAvro.newBuilder()
                        .setId(p.getId())
                        .setType(mapDeviceType(p.getType()))
                        .build();
            }
            case DEVICE_REMOVED -> {
                var p = proto.getDeviceRemoved();
                payload = DeviceRemovedEventAvro.newBuilder()
                        .setId(p.getId())
                        .build();
            }
            case SCENARIO_ADDED -> {
                var p = proto.getScenarioAdded();

                var conditions = p.getConditionsList().stream()
                        .map(c -> ScenarioConditionAvro.newBuilder()
                                .setSensorId(c.getSensorId())
                                .setType(mapConditionType(c.getCondition().getType()))
                                .setOperation(mapConditionOp(c.getCondition().getOperation()))
                                .setValue(c.getCondition().getValue()) // int -> union ok
                                .build())
                        .toList();

                var actions = p.getActionsList().stream()
                        .map(a -> DeviceActionAvro.newBuilder()
                                .setSensorId(a.getSensorId())
                                .setType(mapActionType(a.getAction().getType()))
                                .setValue(a.getAction().getValue())
                                .build())
                        .toList();

                payload = ScenarioAddedEventAvro.newBuilder()
                        .setName(p.getName())
                        .setConditions(conditions)
                        .setActions(actions)
                        .build();
            }
            case SCENARIO_REMOVED -> {
                var p = proto.getScenarioRemoved();
                payload = ScenarioRemovedEventAvro.newBuilder()
                        .setName(p.getName())
                        .build();
            }
            default -> throw new IllegalArgumentException("Unsupported hub payload: " + proto.getPayloadCase());
        }

        builder.setPayload(payload);
        return builder.build();
    }

    private MotionSensorAvro motion(MotionSensorEventDto e) {
        MotionSensorAvro a = new MotionSensorAvro();
        a.setLinkQuality(e.linkQuality());
        a.setMotion(e.motion());
        a.setVoltage(e.voltage());
        return a;
    }

    private TemperatureSensorAvro temperature(TemperatureSensorEventDto e) {
        TemperatureSensorAvro a = new TemperatureSensorAvro();
        a.setTemperatureC(e.temperatureC());
        a.setTemperatureF(e.temperatureF());
        return a;
    }

    private LightSensorAvro light(LightSensorEventDto e) {
        LightSensorAvro a = new LightSensorAvro();
        a.setLinkQuality(e.linkQuality());
        a.setLuminosity(e.luminosity());
        return a;
    }

    private ClimateSensorAvro climate(ClimateSensorEventDto e) {
        ClimateSensorAvro a = new ClimateSensorAvro();
        a.setTemperatureC(e.temperatureC());
        a.setHumidity(e.humidity());
        a.setCo2Level(e.co2Level());
        return a;
    }

    private SwitchSensorAvro sw(SwitchSensorEventDto e) {
        SwitchSensorAvro a = new SwitchSensorAvro();
        a.setState(e.state());
        return a;
    }
}