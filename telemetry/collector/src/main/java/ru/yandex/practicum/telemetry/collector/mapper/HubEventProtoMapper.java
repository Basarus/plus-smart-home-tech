package ru.yandex.practicum.telemetry.collector.mapper;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import ru.yandex.practicum.grpc.telemetry.message.event.HubEventMessagesProto;
import ru.yandex.practicum.grpc.telemetry.message.event.HubEventMessagesProto.HubEvent;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

@Component
public class HubEventProtoMapper {

    public HubEventAvro toHubEventAvro(String hubId, Instant timestamp, byte[] protoPayload) {
        try {
            HubEvent proto = HubEvent.parseFrom(protoPayload);
            return toHubEventAvro(proto);
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot parse HubEvent from bytes", e);
        }
    }

    public HubEventAvro toHubEventAvro(HubEvent proto) {
        HubEventAvro avro = new HubEventAvro();
        avro.setHubId(proto.getHubId());
        avro.setTimestamp(Instant.ofEpochMilli(proto.getTimestamp()));

        Object payload = switch (proto.getPayloadCase()) {
            case DEVICE_ADDED -> mapDeviceAdded(proto.getDeviceAdded());
            case DEVICE_REMOVED -> mapDeviceRemoved(proto.getDeviceRemoved());
            case SCENARIO_ADDED -> mapScenarioAdded(proto.getScenarioAdded());
            case SCENARIO_REMOVED -> mapScenarioRemoved(proto.getScenarioRemoved());
            case PAYLOAD_NOT_SET -> throw new IllegalArgumentException("HubEvent payload is not set");
        };

        avro.setPayload(payload);
        return avro;
    }

    private DeviceAddedEventAvro mapDeviceAdded(HubEventMessagesProto.DeviceAddedEvent p) {
        DeviceAddedEventAvro e = new DeviceAddedEventAvro();
        e.setId(p.getId());
        e.setType(mapDeviceType(p.getDeviceType()));
        return e;
    }

    private DeviceRemovedEventAvro mapDeviceRemoved(HubEventMessagesProto.DeviceRemovedEvent p) {
        DeviceRemovedEventAvro e = new DeviceRemovedEventAvro();
        e.setId(p.getId());
        return e;
    }

    private ScenarioAddedEventAvro mapScenarioAdded(HubEventMessagesProto.ScenarioAddedEvent p) {
        ScenarioAddedEventAvro e = new ScenarioAddedEventAvro();
        e.setName(p.getName());

        List<ScenarioConditionAvro> conditions = new ArrayList<>();
        for (HubEventMessagesProto.ScenarioCondition c : p.getConditionsList()) {
            ScenarioConditionAvro ca = new ScenarioConditionAvro();
            ca.setSensorId(c.getSensorId());
            ca.setType(mapConditionType(c.getType()));
            ca.setOperation(mapConditionOperation(c.getOperation()));

            Object value;
            if (c.getType() == HubEventMessagesProto.ConditionType.MOTION) {
                value = c.getValue() != 0;
            } else {
                value = c.getValue();
            }
            ca.setValue(value);

            conditions.add(ca);
        }
        e.setConditions(conditions);

        List<DeviceActionAvro> actions = new ArrayList<>();
        for (HubEventMessagesProto.ScenarioAction a : p.getActionsList()) {
            DeviceActionAvro aa = new DeviceActionAvro();
            aa.setSensorId(a.getSensorId());
            aa.setType(mapActionType(a.getType()));

            Object value = null;
            if (a.getType() == HubEventMessagesProto.ActionType.SET_VALUE) {
                value = a.getValue();
            }
            aa.setValue(value);

            actions.add(aa);
        }
        e.setActions(actions);

        return e;
    }

    private ScenarioRemovedEventAvro mapScenarioRemoved(HubEventMessagesProto.ScenarioRemovedEvent p) {
        ScenarioRemovedEventAvro e = new ScenarioRemovedEventAvro();
        e.setName(p.getName());
        return e;
    }

    private DeviceTypeAvro mapDeviceType(HubEventMessagesProto.DeviceType t) {
        return switch (t) {
            case SWITCH_SENSOR -> DeviceTypeAvro.SWITCH_SENSOR;
            case MOTION_SENSOR -> DeviceTypeAvro.MOTION_SENSOR;
            case TEMPERATURE_SENSOR -> DeviceTypeAvro.TEMPERATURE_SENSOR;
            case LIGHT_SENSOR -> DeviceTypeAvro.LIGHT_SENSOR;
            case HUMIDITY_SENSOR -> DeviceTypeAvro.HUMIDITY_SENSOR;
            case UNRECOGNIZED -> throw new IllegalArgumentException("Unsupported device type: " + t);
        };
    }

    private ConditionTypeAvro mapConditionType(HubEventMessagesProto.ConditionType t) {
        return switch (t) {
            case MOTION -> ConditionTypeAvro.MOTION;
            case TEMPERATURE -> ConditionTypeAvro.TEMPERATURE;
            case LIGHT -> ConditionTypeAvro.LIGHT;
            case HUMIDITY -> ConditionTypeAvro.HUMIDITY;
            case UNRECOGNIZED -> throw new IllegalArgumentException("Unsupported condition type: " + t);
        };
    }

    private ConditionOperationAvro mapConditionOperation(HubEventMessagesProto.ConditionOperation op) {
        return switch (op) {
            case EQUALS -> ConditionOperationAvro.EQUALS;
            case GREATER_THAN -> ConditionOperationAvro.GREATER_THAN;
            case LOWER_THAN -> ConditionOperationAvro.LOWER_THAN;
            case UNRECOGNIZED -> throw new IllegalArgumentException("Unsupported condition operation: " + op);
        };
    }

    private ActionTypeAvro mapActionType(HubEventMessagesProto.ActionType t) {
        return switch (t) {
            case TURN_ON -> ActionTypeAvro.TURN_ON;
            case TURN_OFF -> ActionTypeAvro.TURN_OFF;
            case SET_VALUE -> ActionTypeAvro.SET_VALUE;
            case UNRECOGNIZED -> throw new IllegalArgumentException("Unsupported action type: " + t);
        };
    }
}