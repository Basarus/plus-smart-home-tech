package ru.yandex.practicum.telemetry.collector.mapper;

import com.google.protobuf.Timestamp;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.message.event.ActionTypeProto;
import ru.yandex.practicum.grpc.telemetry.message.event.ConditionOperationProto;
import ru.yandex.practicum.grpc.telemetry.message.event.ConditionProto;
import ru.yandex.practicum.grpc.telemetry.message.event.ConditionTypeProto;
import ru.yandex.practicum.grpc.telemetry.message.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.message.event.DeviceAddedEventProto;
import ru.yandex.practicum.grpc.telemetry.message.event.DeviceRemovedEventProto;
import ru.yandex.practicum.grpc.telemetry.message.event.DeviceTypeProto;
import ru.yandex.practicum.grpc.telemetry.message.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.message.event.ScenarioActionProto;
import ru.yandex.practicum.grpc.telemetry.message.event.ScenarioAddedEventProto;
import ru.yandex.practicum.grpc.telemetry.message.event.ScenarioConditionProto;
import ru.yandex.practicum.grpc.telemetry.message.event.ScenarioRemovedEventProto;
import ru.yandex.practicum.telemetry.collector.api.dto.DeviceActionDto;
import ru.yandex.practicum.telemetry.collector.api.dto.DeviceAddedEventDto;
import ru.yandex.practicum.telemetry.collector.api.dto.DeviceRemovedEventDto;
import ru.yandex.practicum.telemetry.collector.api.dto.HubEventDto;
import ru.yandex.practicum.telemetry.collector.api.dto.ScenarioAddedEventDto;
import ru.yandex.practicum.telemetry.collector.api.dto.ScenarioConditionDto;
import ru.yandex.practicum.telemetry.collector.api.dto.ScenarioRemovedEventDto;

import java.time.Instant;

@Component
public class HubEventProtoMapper {

    public HubEventProto toProto(HubEventDto dto) {
        HubEventProto.Builder b = HubEventProto.newBuilder()
                .setHubId(dto.getHubId())
                .setTimestamp(toProtoTs(dto.getTimestamp()));

        if (dto instanceof DeviceAddedEventDto e) {
            b.setDeviceAdded(DeviceAddedEventProto.newBuilder()
                    .setId(e.getId())
                    .setType(DeviceTypeProto.valueOf(e.getDeviceType()))
                    .build());
            return b.build();
        }

        if (dto instanceof DeviceRemovedEventDto e) {
            b.setDeviceRemoved(DeviceRemovedEventProto.newBuilder()
                    .setId(e.getId())
                    .build());
            return b.build();
        }

        if (dto instanceof ScenarioRemovedEventDto e) {
            b.setScenarioRemoved(ScenarioRemovedEventProto.newBuilder()
                    .setName(e.getName())
                    .build());
            return b.build();
        }

        if (dto instanceof ScenarioAddedEventDto e) {
            ScenarioAddedEventProto.Builder sb = ScenarioAddedEventProto.newBuilder()
                    .setName(e.getName());

            for (ScenarioConditionDto c : e.getConditions()) {
                sb.addConditions(ScenarioConditionProto.newBuilder()
                        .setSensorId(c.sensorId())
                        .setCondition(ConditionProto.newBuilder()
                                .setType(ConditionTypeProto.valueOf(c.type()))
                                .setOperation(ConditionOperationProto.valueOf(c.operation()))
                                .setValue(c.value())
                                .build())
                        .build());
            }

            for (DeviceActionDto a : e.getActions()) {
                sb.addActions(ScenarioActionProto.newBuilder()
                        .setSensorId(a.sensorId())
                        .setAction(DeviceActionProto.newBuilder()
                                .setType(ActionTypeProto.valueOf(a.type()))
                                .setValue(a.value())
                                .build())
                        .build());
            }

            b.setScenarioAdded(sb.build());
            return b.build();
        }

        throw new IllegalArgumentException("Unsupported hub event: " + dto.getClass().getName());
    }

    private Timestamp toProtoTs(Instant instant) {
        return Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();
    }
}