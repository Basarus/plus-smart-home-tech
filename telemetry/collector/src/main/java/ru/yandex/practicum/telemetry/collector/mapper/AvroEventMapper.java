package ru.yandex.practicum.telemetry.collector.mapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.grpc.telemetry.message.event.*;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.telemetry.collector.api.dto.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
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

    public SensorEventAvro toAvro(SensorEventProto request) {
        SensorEventAvro avro = new SensorEventAvro();
        avro.setId(request.getId());
        avro.setHubId(request.getHubId());
        avro.setTimestamp(toInstant(request.getTimestamp()));

        Object payload = switch (request.getPayloadCase()) {
            case MOTION_SENSOR -> {
                var p = request.getMotionSensor();
                MotionSensorAvro a = new MotionSensorAvro();
                a.setLinkQuality(p.getLinkQuality());
                a.setMotion(p.getMotion());
                a.setVoltage(p.getVoltage());
                yield a;
            }
            case TEMPERATURE_SENSOR -> {
                var p = request.getTemperatureSensor();
                TemperatureSensorAvro a = new TemperatureSensorAvro();
                a.setTemperatureC(p.getTemperatureC());
                a.setTemperatureF(p.getTemperatureF());
                yield a;
            }
            case LIGHT_SENSOR -> {
                var p = request.getLightSensor();
                LightSensorAvro a = new LightSensorAvro();
                a.setLinkQuality(p.getLinkQuality());
                a.setLuminosity(p.getLuminosity());
                yield a;
            }
            case CLIMATE_SENSOR -> {
                var p = request.getClimateSensor();
                ClimateSensorAvro a = new ClimateSensorAvro();
                a.setTemperatureC(p.getTemperatureC());
                a.setHumidity(p.getHumidity());
                yield a;
            }
            case SWITCH_SENSOR -> {
                var p = request.getSwitchSensor();
                SwitchSensorAvro a = new SwitchSensorAvro();
                a.setState(p.getState());
                yield a;
            }
            case PAYLOAD_NOT_SET -> throw new IllegalArgumentException("Sensor payload is not set");
        };

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
            case DEVICE_ACTION_REQUEST -> throw new IllegalArgumentException("DeviceActionRequest is not a hub event for Collector storage");
            case PAYLOAD_NOT_SET -> throw new IllegalArgumentException("Hub payload is not set");
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

    private Instant toInstant(com.google.protobuf.Timestamp ts) {
        return Instant.ofEpochSecond(ts.getSeconds(), ts.getNanos());
    }

    private DeviceTypeAvro mapDeviceType(DeviceTypeProto t) {
        if (t == null || t == DeviceTypeProto.UNRECOGNIZED) {
            throw new IllegalArgumentException("Unsupported device type: " + t);
        }

        return switch (t) {
            case TEMPERATURE_SENSOR -> DeviceTypeAvro.TEMPERATURE_SENSOR;
            case MOTION_SENSOR -> DeviceTypeAvro.MOTION_SENSOR;
            case LIGHT_SENSOR -> DeviceTypeAvro.LIGHT_SENSOR;
            case SWITCH_SENSOR -> DeviceTypeAvro.SWITCH_SENSOR;
            case CLIMATE_SENSOR -> DeviceTypeAvro.CLIMATE_SENSOR;
            default -> throw new IllegalArgumentException("Unsupported device type for Avro: " + t);
        };
    }

    private DeviceTypeAvro mapDeviceType(String s) {
        if (s == null || s.isBlank()) return null;

        return switch (s) {
            case "TEMPERATURE_SENSOR" -> DeviceTypeAvro.TEMPERATURE_SENSOR;
            case "MOTION_SENSOR" -> DeviceTypeAvro.MOTION_SENSOR;
            case "LIGHT_SENSOR" -> DeviceTypeAvro.LIGHT_SENSOR;
            case "SWITCH_SENSOR" -> DeviceTypeAvro.SWITCH_SENSOR;
            case "CLIMATE_SENSOR" -> DeviceTypeAvro.CLIMATE_SENSOR;
            default -> throw new IllegalArgumentException("Unsupported device type: " + s);
        };
    }

    private ConditionTypeAvro mapConditionType(ConditionTypeProto t) {
        return switch (t) {
            case MOTION -> ConditionTypeAvro.MOTION;
            case TEMPERATURE -> ConditionTypeAvro.TEMPERATURE;
            case ILLUMINATION -> ConditionTypeAvro.ILLUMINATION;
            case HUMIDITY -> ConditionTypeAvro.HUMIDITY;
            case SWITCH -> ConditionTypeAvro.SWITCH;
            case CO2LEVEL -> ConditionTypeAvro.CO2LEVEL;
            case UNRECOGNIZED -> null;
        };
    }

    private ConditionOperationAvro mapConditionOperation(ConditionOperationProto op) {
        return switch (op) {
            case EQUALS -> ConditionOperationAvro.EQUALS;
            case GREATER_THAN -> ConditionOperationAvro.GREATER_THAN;
            case LOWER_THAN -> ConditionOperationAvro.LOWER_THAN;
            case UNRECOGNIZED -> null;
        };
    }

    private Object mapConditionValue(ConditionProto cond) {
        return switch (cond.getValueCase()) {
            case INT_VALUE -> cond.getIntValue();
            case BOOL_VALUE -> cond.getBoolValue();
            case VALUE_NOT_SET -> null;
        };
    }

    private ActionTypeAvro mapActionType(ActionTypeProto t) {
        if (t == null || t == ActionTypeProto.UNRECOGNIZED) {
            return null;
        }

        return switch (t) {
            case ACTIVATE -> ActionTypeAvro.ACTIVATE;
            case DEACTIVATE -> ActionTypeAvro.DEACTIVATE;
            case INVERSE -> ActionTypeAvro.INVERSE;
            case SET_VALUE -> ActionTypeAvro.SET_VALUE;
            case UNRECOGNIZED -> null;
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