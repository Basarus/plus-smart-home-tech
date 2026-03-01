package ru.yandex.practicum.telemetry.collector.mapper;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Schema;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.*;
import ru.yandex.practicum.grpc.telemetry.message.event.*;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class AvroEventMapper {

    public SensorEventAvro toAvro(SensorEventProto request) {
        return toSensorEventAvro(request);
    }

    public HubEventAvro toAvro(HubEventProto request) {
        return toHubEventAvro(request);
    }

    public SensorEventAvro toSensorEventAvro(SensorEventProto request) {
        SensorEventAvro avro = new SensorEventAvro();
        avro.setId(request.getId());
        avro.setHubId(request.getHubId());
        avro.setTimestamp(toInstant(request.getTimestamp()));

        Object payload;
        switch (request.getPayloadCase()) {
            case CLIMATE_SENSOR -> payload = mapClimate(request.getClimateSensor());
            case LIGHT_SENSOR -> payload = mapLight(request.getLightSensor());
            case MOTION_SENSOR -> payload = mapMotion(request.getMotionSensor());
            case SWITCH_SENSOR -> payload = mapSwitch(request.getSwitchSensor());
            case TEMPERATURE_SENSOR -> payload = mapTemperature(request.getTemperatureSensor());
            case PAYLOAD_NOT_SET -> payload = null;
            default -> payload = null;
        }

        avro.setPayload(payload);
        return avro;
    }

    public HubEventAvro toHubEventAvro(HubEventProto request) {
        HubEventAvro avro = new HubEventAvro();
        avro.setHubId(request.getHubId());
        avro.setTimestamp(toInstant(request.getTimestamp()));

        Object payload;
        switch (request.getPayloadCase()) {
            case DEVICE_ADDED -> payload = mapDeviceAdded(request.getDeviceAdded());
            case DEVICE_REMOVED -> payload = mapDeviceRemoved(request.getDeviceRemoved());
            case SCENARIO_ADDED -> payload = mapScenarioAdded(request.getScenarioAdded());
            case SCENARIO_REMOVED -> payload = mapScenarioRemoved(request.getScenarioRemoved());
            case PAYLOAD_NOT_SET -> payload = null;
            default -> payload = null;
        }

        avro.setPayload(payload);
        return avro;
    }

    private ClimateSensorAvro mapClimate(ClimateSensorProto p) {
        ClimateSensorAvro a = new ClimateSensorAvro();
        putIfPresent(a, "temperatureC", p.getTemperatureC());
        putIfPresent(a, "humidity", p.getHumidity());
        putIfPresent(a, "co2Level", p.getCo2Level());
        putIfPresent(a, "co2", p.getCo2Level());
        putIfPresent(a, "co2level", p.getCo2Level());
        return a;
    }

    private LightSensorAvro mapLight(LightSensorProto p) {
        LightSensorAvro a = new LightSensorAvro();
        putIfPresent(a, "linkQuality", p.getLinkQuality());
        putIfPresent(a, "luminosity", p.getLuminosity());
        return a;
    }

    private MotionSensorAvro mapMotion(MotionSensorProto p) {
        MotionSensorAvro a = new MotionSensorAvro();
        putIfPresent(a, "linkQuality", p.getLinkQuality());
        putIfPresent(a, "motion", p.getMotion());
        putIfPresent(a, "voltage", p.getVoltage());
        return a;
    }

    private SwitchSensorAvro mapSwitch(SwitchSensorProto p) {
        SwitchSensorAvro a = new SwitchSensorAvro();
        putIfPresent(a, "state", p.getState());
        return a;
    }

    private TemperatureSensorAvro mapTemperature(TemperatureSensorProto p) {
        TemperatureSensorAvro a = new TemperatureSensorAvro();
        putIfPresent(a, "temperatureC", p.getTemperatureC());
        putIfPresent(a, "temperatureF", p.getTemperatureF());
        return a;
    }

    private DeviceAddedEventAvro mapDeviceAdded(DeviceAddedEventProto proto) {
        DeviceAddedEventAvro a = new DeviceAddedEventAvro();
        a.setId(proto.getId());
        a.setType(mapDeviceType(proto.getType()));
        return a;
    }

    private DeviceRemovedEventAvro mapDeviceRemoved(DeviceRemovedEventProto proto) {
        DeviceRemovedEventAvro a = new DeviceRemovedEventAvro();
        a.setId(proto.getId());
        return a;
    }

    private ScenarioAddedEventAvro mapScenarioAdded(ScenarioAddedEventProto proto) {
        ScenarioAddedEventAvro a = new ScenarioAddedEventAvro();
        a.setName(proto.getName());
        a.setConditions(mapConditions(proto.getConditionsList()));
        a.setActions(mapActions(proto.getActionsList()));
        return a;
    }

    private ScenarioRemovedEventAvro mapScenarioRemoved(ScenarioRemovedEventProto proto) {
        ScenarioRemovedEventAvro a = new ScenarioRemovedEventAvro();
        a.setName(proto.getName());
        return a;
    }

    private List<ScenarioConditionAvro> mapConditions(List<ScenarioConditionProto> items) {
        List<ScenarioConditionAvro> out = new ArrayList<>();
        for (ScenarioConditionProto proto : items) {
            ScenarioConditionAvro a = new ScenarioConditionAvro();
            a.setSensorId(proto.getSensorId());

            ConditionProto cond = safeInvoke(proto, ConditionProto.class, "getCondition", "getCond", "getConditionProto");
            if (cond != null) {
                a.setType(mapConditionType(cond.getType()));
                a.setOperation(mapConditionOperation(cond.getOperation()));
                a.setValue(mapConditionValueAsInt(cond));
            } else {
                ConditionTypeProto t = safeInvoke(proto, ConditionTypeProto.class, "getType");
                ConditionOperationProto op = safeInvoke(proto, ConditionOperationProto.class, "getOperation");
                if (t != null) a.setType(mapConditionType(t));
                if (op != null) a.setOperation(mapConditionOperation(op));
                Integer v = safeInvoke(proto, Integer.class, "getValue", "getIntValue");
                a.setValue(v != null ? v : 0);
            }

            out.add(a);
        }
        return out;
    }

    private int mapConditionValueAsInt(ConditionProto cond) {
        return switch (cond.getValueCase()) {
            case INT_VALUE -> cond.getIntValue();
            case BOOL_VALUE -> cond.getBoolValue() ? 1 : 0;
            case VALUE_NOT_SET -> 0;
            default -> 0;
        };
    }

    private List<DeviceActionAvro> mapActions(List<ScenarioActionProto> items) {
        List<DeviceActionAvro> out = new ArrayList<>();
        for (ScenarioActionProto proto : items) {
            DeviceActionAvro a = new DeviceActionAvro();

            a.setSensorId(proto.getSensorId());
            a.setType(mapActionType(proto.getAction().getType()));

            if (proto.getAction().getType() == ActionTypeProto.SET_VALUE) {
                a.setValue(proto.getAction().getValue());
            } else {
                a.setValue(0);
            }

            out.add(a);
        }
        return out;
    }

    private DeviceTypeAvro mapDeviceType(DeviceTypeProto t) {
        return switch (t) {
            case MOTION_SENSOR -> DeviceTypeAvro.MOTION_SENSOR;
            case TEMPERATURE_SENSOR -> DeviceTypeAvro.TEMPERATURE_SENSOR;
            case LIGHT_SENSOR -> DeviceTypeAvro.LIGHT_SENSOR;
            case CLIMATE_SENSOR -> DeviceTypeAvro.CLIMATE_SENSOR;
            case SWITCH_SENSOR -> DeviceTypeAvro.SWITCH_SENSOR;
            case UNRECOGNIZED -> null;
        };
    }

    private ConditionTypeAvro mapConditionType(ConditionTypeProto t) {
        return switch (t) {
            case MOTION -> ConditionTypeAvro.MOTION;
            case TEMPERATURE -> ConditionTypeAvro.TEMPERATURE;
            case LUMINOSITY -> ConditionTypeAvro.LUMINOSITY;
            case CO2LEVEL -> ConditionTypeAvro.CO2LEVEL;
            case HUMIDITY -> ConditionTypeAvro.HUMIDITY;
            case SWITCH -> ConditionTypeAvro.SWITCH;
            case UNRECOGNIZED -> null;
        };
    }

    private ConditionOperationAvro mapConditionOperation(ConditionOperationProto t) {
        return switch (t) {
            case EQUALS -> ConditionOperationAvro.EQUALS;
            case GREATER_THAN -> ConditionOperationAvro.GREATER_THAN;
            case LOWER_THAN -> ConditionOperationAvro.LOWER_THAN;
            case UNRECOGNIZED -> null;
        };
    }

    private ActionTypeAvro mapActionType(ActionTypeProto t) {
        if (t == null) return null;

        return switch (t) {
            case ACTIVATE -> firstExistingEnum(ActionTypeAvro.class, "ACTIVATE", "TURN_ON", "ON", "ENABLE");
            case DEACTIVATE -> firstExistingEnum(ActionTypeAvro.class, "DEACTIVATE", "TURN_OFF", "OFF", "DISABLE");
            case INVERSE -> firstExistingEnum(ActionTypeAvro.class, "INVERSE", "TOGGLE", "SWITCH");
            case SET_VALUE -> firstExistingEnum(ActionTypeAvro.class, "SET_VALUE", "SET", "VALUE");
            case UNRECOGNIZED -> null;
        };
    }

    private Instant toInstant(com.google.protobuf.Timestamp ts) {
        return Instant.ofEpochSecond(ts.getSeconds(), ts.getNanos());
    }

    private void putIfPresent(SpecificRecordBase record, String fieldName, Object value) {
        if (record == null || fieldName == null) return;
        Schema.Field f = record.getSchema().getField(fieldName);
        if (f == null) return;
        record.put(f.pos(), value);
    }

    private <T> T safeInvoke(Object target, Class<T> returnType, String... methodNames) {
        if (target == null || methodNames == null) return null;
        for (String name : methodNames) {
            if (name == null) continue;
            try {
                Method m = target.getClass().getMethod(name);
                Object res = m.invoke(target);
                if (res == null) continue;

                if (returnType.isInstance(res)) return returnType.cast(res);

                if (returnType == Integer.class && res instanceof Number n) return returnType.cast(n.intValue());
                if (returnType == Long.class && res instanceof Number n) return returnType.cast(n.longValue());

            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private <E extends Enum<E>> E firstExistingEnum(Class<E> enumClass, String... candidates) {
        if (enumClass == null || candidates == null) return null;
        for (String c : candidates) {
            if (c == null) continue;
            try {
                return Enum.valueOf(enumClass, c);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private long toEpochMillis(com.google.protobuf.Timestamp ts) {
        return ts.getSeconds() * 1000L + ts.getNanos() / 1_000_000L;
    }
}