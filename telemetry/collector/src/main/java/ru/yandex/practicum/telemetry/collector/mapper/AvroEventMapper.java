package ru.yandex.practicum.telemetry.collector.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.message.event.*;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.telemetry.collector.api.dto.*;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

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

    public HubEventAvro toHubEventAvro(HubEventProto request) {
        HubEventAvro avro = new HubEventAvro();
        avro.setHubId(request.getHubId());
        avro.setTimestamp(toInstant(request.getTimestamp()));

        switch (request.getPayloadCase()) {
            case DEVICE_ADDED -> {
                var p = request.getDeviceAdded();
                DeviceAddedEventAvro payload = new DeviceAddedEventAvro();
                payload.setId(p.getId());
                payload.setType(mapDeviceType(p.getType()));
                avro.setPayload(payload);
            }
            case DEVICE_REMOVED -> {
                var p = request.getDeviceRemoved();
                DeviceRemovedEventAvro payload = new DeviceRemovedEventAvro();
                payload.setId(p.getId());
                avro.setPayload(payload);
            }
            case SCENARIO_ADDED -> {
                var p = request.getScenarioAdded();
                ScenarioAddedEventAvro payload = new ScenarioAddedEventAvro();
                payload.setName(p.getName());
                payload.setConditions(mapConditions(p.getConditionsList()));
                payload.setActions(mapActions(p.getActionsList()));
                avro.setPayload(payload);
            }
            case SCENARIO_REMOVED -> {
                var p = request.getScenarioRemoved();
                ScenarioRemovedEventAvro payload = new ScenarioRemovedEventAvro();
                payload.setName(p.getName());
                avro.setPayload(payload);
            }
            default -> throw new IllegalArgumentException("Unsupported hub payload: " + request.getPayloadCase());
        }

        return avro;
    }

    private Instant toInstant(com.google.protobuf.Timestamp ts) {
        return Instant.ofEpochSecond(ts.getSeconds(), ts.getNanos());
    }

    private DeviceTypeAvro mapDeviceType(DeviceTypeProto t) {
        int n = t.getNumber();
        return switch (n) {
            case 1 -> DeviceTypeAvro.valueOf("MOTION_SENSOR");
            case 2 -> DeviceTypeAvro.valueOf("TEMPERATURE_SENSOR");
            case 3 -> DeviceTypeAvro.valueOf("LIGHT_SENSOR");
            case 4 -> DeviceTypeAvro.valueOf("HUMIDITY_SENSOR");
            case 5 -> DeviceTypeAvro.valueOf("SWITCH_SENSOR");
            default -> throw new IllegalArgumentException("Unsupported device type number for Avro: " + n);
        };
    }

    private List<ScenarioConditionAvro> mapConditions(List<ScenarioConditionProto> conditions) {
        List<ScenarioConditionAvro> out = new ArrayList<>(conditions.size());
        for (ScenarioConditionProto c : conditions) {
            ScenarioConditionAvro a = new ScenarioConditionAvro();
            a.setSensorId(c.getSensorId());

            ConditionProto cond = c.getCondition();

            ConditionTypeProto ct = cond.getType();
            a.setType(mapConditionType(ct));
            a.setOperation(mapConditionOperation(cond.getOperation()));

            Object valueObj = readConditionValue(cond, ct);
            a.setValue(valueObj);

            out.add(a);
        }
        return out;
    }

    private Object readConditionValue(ConditionProto cond, ConditionTypeProto type) {
        Object fromOneof = tryReadConditionValueFromOneof(cond, type);
        if (fromOneof != null || hasValueCase(cond)) {
            return fromOneof;
        }

        Integer fromGetValue = tryReadConditionValueFromGetValue(cond);
        if (fromGetValue == null) {
            return null;
        }

        return castConditionValue(type, fromGetValue);
    }

    private boolean hasValueCase(ConditionProto cond) {
        try {
            cond.getClass().getMethod("getValueCase");
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    private Object tryReadConditionValueFromOneof(ConditionProto cond, ConditionTypeProto type) {
        try {
            Method getValueCase = cond.getClass().getMethod("getValueCase");
            Object valueCase = getValueCase.invoke(cond);
            String name = String.valueOf(valueCase);

            if ("VALUE_NOT_SET".equals(name)) {
                return null;
            }

            if ("INT_VALUE".equals(name)) {
                Method getIntValue = cond.getClass().getMethod("getIntValue");
                int v = (int) getIntValue.invoke(cond);
                return castConditionValue(type, v);
            }

            if ("BOOL_VALUE".equals(name)) {
                Method getBoolValue = cond.getClass().getMethod("getBoolValue");
                boolean v = (boolean) getBoolValue.invoke(cond);
                return v;
            }

            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private Integer tryReadConditionValueFromGetValue(ConditionProto cond) {
        try {
            Method getValue = cond.getClass().getMethod("getValue");
            Object v = getValue.invoke(cond);
            if (v instanceof Integer i) return i;
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private Object castConditionValue(ConditionTypeProto type, int v) {
        int n = type.getNumber();
        if (n == 4 || n == 5) {
            return Boolean.valueOf(v != 0);
        }
        return Integer.valueOf(v);
    }

    private List<DeviceActionAvro> mapActions(List<ScenarioActionProto> actions) {
        List<DeviceActionAvro> out = new ArrayList<>(actions.size());
        for (ScenarioActionProto act : actions) {
            DeviceActionAvro a = new DeviceActionAvro();
            a.setSensorId(act.getSensorId());

            DeviceActionProto proto = act.getAction();
            ActionTypeAvro type = mapActionType(proto.getType());
            a.setType(type);

            if (isSetValue(proto.getType())) {
                a.setValue(proto.getValue());
            } else {
                a.setValue(0);
            }

            out.add(a);
        }
        return out;
    }

    private boolean isSetValue(ActionTypeProto t) {
        int n = t.getNumber();
        return n == 3 || n == 4 || n == 5;
    }

    private ConditionTypeAvro mapConditionType(ConditionTypeProto t) {
        int n = t.getNumber();
        return switch (n) {
            case 1 -> ConditionTypeAvro.valueOf("TEMPERATURE");
            case 2 -> ConditionTypeAvro.valueOf("HUMIDITY");
            case 3 -> ConditionTypeAvro.valueOf("LIGHT");
            case 4 -> ConditionTypeAvro.valueOf("MOTION");
            case 5 -> ConditionTypeAvro.valueOf("MOTION");
            case 6 -> ConditionTypeAvro.valueOf("TEMPERATURE");
            default -> throw new IllegalArgumentException("Unsupported condition type number: " + n);
        };
    }

    private ConditionOperationAvro mapConditionOperation(ConditionOperationProto op) {
        return switch (op.getNumber()) {
            case 1 -> ConditionOperationAvro.EQUALS;
            case 2 -> ConditionOperationAvro.GREATER_THAN;
            case 3 -> ConditionOperationAvro.LOWER_THAN;
            default -> throw new IllegalArgumentException("Unsupported condition operation: " + op);
        };
    }

    private ActionTypeAvro mapActionType(ActionTypeProto t) {
        return switch (t.getNumber()) {
            case 1 -> ActionTypeAvro.valueOf("TURN_ON");
            case 2 -> ActionTypeAvro.valueOf("TURN_OFF");
            case 3, 4, 5 -> ActionTypeAvro.valueOf("SET_VALUE");
            default -> throw new IllegalArgumentException("Unsupported action type number: " + t.getNumber());
        };
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