package ru.yandex.practicum.telemetry.collector.service;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.message.event.HubEventMessagesProto;
import ru.yandex.practicum.telemetry.collector.api.dto.*;

import java.time.Instant;

import static ru.yandex.practicum.grpc.telemetry.message.event.HubEventMessagesProto.*;

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

    private com.google.protobuf.Timestamp toProtoTs(Instant instant) {
        return com.google.protobuf.Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();
    }
}