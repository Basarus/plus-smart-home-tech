package ru.yandex.practicum.telemetry.collector.mapper;

import com.google.protobuf.Timestamp;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.message.event.*;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.telemetry.collector.api.dto.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static ru.yandex.practicum.grpc.telemetry.event.SensorEventProto.PayloadCase.CLIMATE_SENSOR;
import static ru.yandex.practicum.kafka.telemetry.event.ActionTypeAvro.*;

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
            case DEVICE_ADDED -> avro.setPayload(mapDeviceAdded(request.getDeviceAdded()));
            case DEVICE_REMOVED -> avro.setPayload(mapDeviceRemoved(request.getDeviceRemoved()));
            case SCENARIO_ADDED -> avro.setPayload(mapScenarioAdded(request.getScenarioAdded()));
            case SCENARIO_REMOVED -> avro.setPayload(mapScenarioRemoved(request.getScenarioRemoved()));
            case DEVICE_ACTION_REQUEST, PAYLOAD_NOT_SET ->
                    throw new IllegalArgumentException("Unsupported hub payload: " + request.getPayloadCase());
        }

        return avro;
    }

    public HubEventAvro toHubEventAvro(HubEventDto dto) {
        HubEventAvro avro = new HubEventAvro();
        avro.setHubId(dto.getHubId());
        avro.setTimestamp(dto.getTimestamp());

        if (dto instanceof DeviceAddedEventDto e) {
            DeviceAddedEventAvro payload = new DeviceAddedEventAvro();
            payload.setId(e.getId());
            payload.setType(mapDeviceType(e.getDeviceType()));
            avro.setPayload(payload);
            return avro;
        }

        if (dto instanceof DeviceRemovedEventDto e) {
            DeviceRemovedEventAvro payload = new DeviceRemovedEventAvro();
            payload.setId(e.getId());
            avro.setPayload(payload);
            return avro;
        }

        if (dto instanceof ScenarioAddedEventDto e) {
            ScenarioAddedEventAvro payload = new ScenarioAddedEventAvro();
            payload.setName(e.getName());
            payload.setConditions(new ArrayList<>());
            payload.setActions(new ArrayList<>());
            avro.setPayload(payload);
            return avro;
        }

        if (dto instanceof ScenarioRemovedEventDto e) {
            ScenarioRemovedEventAvro payload = new ScenarioRemovedEventAvro();
            payload.setName(e.getName());
            avro.setPayload(payload);
            return avro;
        }

        throw new IllegalArgumentException("Unsupported hub event: " + dto.getClass().getName());
    }

    private DeviceAddedEventAvro mapDeviceAdded(DeviceAddedEventProto p) {
        DeviceAddedEventAvro payload = new DeviceAddedEventAvro();
        payload.setId(p.getId());
        payload.setType(mapDeviceType(p.getType()));
        return payload;
    }

    private DeviceRemovedEventAvro mapDeviceRemoved(DeviceRemovedEventProto p) {
        DeviceRemovedEventAvro payload = new DeviceRemovedEventAvro();
        payload.setId(p.getId());
        return payload;
    }

    private ScenarioAddedEventAvro mapScenarioAdded(ScenarioAddedEventProto p) {
        ScenarioAddedEventAvro payload = new ScenarioAddedEventAvro();
        payload.setName(p.getName());
        payload.setConditions(mapConditions(p.getConditionsList()));
        payload.setActions(mapActions(p.getActionsList()));
        return payload;
    }

    private ScenarioRemovedEventAvro mapScenarioRemoved(ScenarioRemovedEventProto p) {
        ScenarioRemovedEventAvro payload = new ScenarioRemovedEventAvro();
        payload.setName(p.getName());
        return payload;
    }

    private Instant toInstant(Timestamp ts) {
        if (ts == null) return Instant.EPOCH;
        return Instant.ofEpochSecond(ts.getSeconds(), ts.getNanos());
    }

    private DeviceTypeAvro mapDeviceType(DeviceTypeProto t) {
        return switch (t) {
            case MOTION_SENSOR -> DeviceTypeAvro.MOTION_SENSOR;
            case TEMPERATURE_SENSOR -> DeviceTypeAvro.TEMPERATURE_SENSOR;
            case LIGHT_SENSOR -> DeviceTypeAvro.LIGHT_SENSOR;
            case CLIMATE_SENSOR -> DeviceTypeAvro.CLIMATE_SENSOR;
            case SWITCH_SENSOR -> DeviceTypeAvro.SWITCH_SENSOR;
            case UNRECOGNIZED -> throw new IllegalArgumentException("Unknown deviceType: " + t);
            default -> throw new IllegalArgumentException("Unsupported deviceType for avro: " + t);
        };
    }

    private DeviceTypeAvro mapDeviceType(String s) {
        if (s == null) throw new IllegalArgumentException("deviceType is null");

        return switch (s) {
            case "MOTION_SENSOR" -> DeviceTypeAvro.MOTION_SENSOR;
            case "TEMPERATURE_SENSOR" -> DeviceTypeAvro.TEMPERATURE_SENSOR;
            case "LIGHT_SENSOR" -> DeviceTypeAvro.LIGHT_SENSOR;
            case "CLIMATE_SENSOR" -> DeviceTypeAvro.CLIMATE_SENSOR;
            case "HUMIDITY_SENSOR" -> DeviceTypeAvro.HUMIDITY_SENSOR;
            case "SWITCH_SENSOR" -> DeviceTypeAvro.SWITCH_SENSOR;
            default -> throw new IllegalArgumentException("Unknown deviceType: " + s);
        };
    }

    private List<ScenarioConditionAvro> mapConditions(List<ScenarioConditionProto> conditions) {
        List<ScenarioConditionAvro> out = new ArrayList<>(conditions.size());
        for (ScenarioConditionProto c : conditions) {
            ScenarioConditionAvro a = new ScenarioConditionAvro();
            a.setSensorId(c.getSensorId());

            ConditionProto cond = c.getCondition();
            a.setType(mapConditionType(cond.getType()));
            a.setOperation(mapConditionOperation(cond.getOperation()));
            a.setValue(mapConditionValue(cond));

            out.add(a);
        }
        return out;
    }

    private Object mapConditionValue(ConditionProto cond) {
        try {
            var m = cond.getClass().getMethod("getValue");
            return m.invoke(cond);
        } catch (NoSuchMethodException ignored) {
            try {
                var mCase = cond.getClass().getMethod("getValueCase");
                Object valueCase = mCase.invoke(cond);
                String name = String.valueOf(valueCase);

                if ("INT_VALUE".equals(name)) {
                    return cond.getClass().getMethod("getIntValue").invoke(cond);
                }
                if ("BOOL_VALUE".equals(name)) {
                    return cond.getClass().getMethod("getBoolValue").invoke(cond);
                }
                return null;
            } catch (Exception e) {
                throw new IllegalArgumentException("Unsupported ConditionProto value model: " + cond.getClass().getName(), e);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to read ConditionProto value", e);
        }
    }

    private List<DeviceActionAvro> mapActions(List<ScenarioActionProto> actions) {
        List<DeviceActionAvro> out = new ArrayList<>(actions.size());

        for (ScenarioActionProto act : actions) {
            DeviceActionAvro a = new DeviceActionAvro();
            a.setSensorId(act.getSensorId());

            DeviceActionProto proto = act.getAction();
            a.setType(mapActionType(proto.getType()));
            a.setValue(proto.getValue());

            out.add(a);
        }

        return out;
    }

    private ConditionTypeAvro mapConditionType(ConditionTypeProto t) {
        return switch (t) {
            case MOTION -> ConditionTypeAvro.MOTION;
            case TEMPERATURE -> ConditionTypeAvro.TEMPERATURE;
            case HUMIDITY -> ConditionTypeAvro.HUMIDITY;
            case ILLUMINATION -> ConditionTypeAvro.LIGHT;
            case UNRECOGNIZED -> throw new IllegalArgumentException("Unknown conditionType: " + t);
            default -> throw new IllegalArgumentException("Unsupported conditionType for avro: " + t);
        };
    }

    private ConditionOperationAvro mapConditionOperation(ConditionOperationProto op) {
        return switch (op) {
            case LOWER_THAN -> ConditionOperationAvro.LOWER_THAN;
            case GREATER_THAN -> ConditionOperationAvro.GREATER_THAN;
            case EQUALS -> ConditionOperationAvro.EQUALS;
            case UNRECOGNIZED -> throw new IllegalArgumentException("Unknown conditionOperation: " + op);
            default -> throw new IllegalArgumentException("Unsupported conditionOperation for avro: " + op);
        };
    }

    private ActionTypeAvro mapActionType(ActionTypeProto t) {
        return switch (t) {
            case ACTIVATE -> ACTIVATE;
            case DEACTIVATE -> DEACTIVATE;
            case INVERSE -> INVERSE;
            case UNRECOGNIZED -> throw new IllegalArgumentException("Unknown actionType: " + t);
            default -> throw new IllegalArgumentException("Unsupported actionType for avro: " + t);
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