package ru.yandex.practicum.telemetry.collector.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.message.event.*;
import ru.yandex.practicum.telemetry.collector.api.dto.*;

import java.time.Instant;

@Component
public class HubEventProtoMapper {

    public HubEventProto toProto(HubEventDto dto) {
        HubEventProto.Builder b = HubEventProto.newBuilder()
                .setHubId(dto.getHubId())
                .setTimestamp(toTs(dto.getTimestamp()));

        if (dto instanceof DeviceAddedEventDto e) {
            b.setDeviceAdded(DeviceAddedEventProto.newBuilder()
                    .setId(e.getId())
                    .setType(toDeviceType(e.getDeviceType()))
                    .build());
        } else if (dto instanceof DeviceRemovedEventDto e) {
            b.setDeviceRemoved(DeviceRemovedEventProto.newBuilder()
                    .setId(e.getId())
                    .build());
        } else if (dto instanceof ScenarioAddedEventDto e) {
            b.setScenarioAdded(ScenarioAddedEventProto.newBuilder()
                    .setName(e.getName())
                    .build());
        } else if (dto instanceof ScenarioRemovedEventDto e) {
            b.setScenarioRemoved(ScenarioRemovedEventProto.newBuilder()
                    .setName(e.getName())
                    .build());
        } else {
            throw new IllegalArgumentException("Unsupported hub event: " + dto.getClass().getName());
        }

        return b.build();
    }

    private com.google.protobuf.Timestamp toTs(Instant i) {
        return com.google.protobuf.Timestamp.newBuilder()
                .setSeconds(i.getEpochSecond())
                .setNanos(i.getNano())
                .build();
    }

    private DeviceTypeProto toDeviceType(String s) {
        if (s == null || s.isBlank()) {
            throw new IllegalArgumentException("Device type is null/blank");
        }

        return switch (s) {
            case "MOTION_SENSOR" -> DeviceTypeProto.MOTION_SENSOR;
            case "TEMPERATURE_SENSOR" -> DeviceTypeProto.TEMPERATURE_SENSOR;
            case "LIGHT_SENSOR" -> DeviceTypeProto.LIGHT_SENSOR;
            case "HUMIDITY_SENSOR" -> DeviceTypeProto.HUMIDITY_SENSOR;
            case "SWITCH_SENSOR" -> DeviceTypeProto.SWITCH_SENSOR;
            default -> throw new IllegalArgumentException("Unsupported device type: " + s);
        };
    }
}