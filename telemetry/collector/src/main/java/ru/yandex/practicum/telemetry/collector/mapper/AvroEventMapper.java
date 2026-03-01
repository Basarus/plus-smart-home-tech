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
        log.debug("Mapping SensorEventDto: type={}, id={}, hubId={}", dto.getClass().getSimpleName(), dto.getId(), dto.getHubId());

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

        log.debug("Mapped SensorEventAvro: id={}, hubId={}, payloadType={}", avro.getId(), avro.getHubId(), payload.getClass().getSimpleName());
        return avro;
    }

    public SensorEventAvro toAvro(SensorEventProto request) {
        log.debug("Mapping SensorEventProto: id={}, hubId={}, payloadCase={}", request.getId(), request.getHubId(), request.getPayloadCase());

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
                a.setCo2Level(p.getCo2Level());
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

        log.debug("Mapped SensorEventAvro from proto: id={}, hubId={}, payloadType={}", avro.getId(), avro.getHubId(), payload.getClass().getSimpleName());
        return avro;
    }

    public HubEventAvro toHubEventAvro(HubEventProto request) {
        log.debug("Mapping HubEventProto: hubId={}, payloadCase={}", request.getHubId(), request.getPayloadCase());

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
                log.debug("Mapped DEVICE_ADDED: id={}, type={}", payload.getId(), payload.getType());
            }
            case DEVICE_REMOVED -> {
                var p = request.getDeviceRemoved();
                DeviceRemovedEventAvro payload = new DeviceRemovedEventAvro();
                payload.setId(p.getId());
                avro.setPayload(payload);
                log.debug("Mapped DEVICE_REMOVED: id={}", payload.getId());
            }
            case SCENARIO_ADDED -> {
                var p = request.getScenarioAdded();
                ScenarioAddedEventAvro payload = new ScenarioAddedEventAvro();
                payload.setName(p.getName());

                log.debug("SCENARIO_ADDED: name={}, conditions count={}, actions count={}", p.getName(), p.getConditionsList().size(), p.getActionsList().size());

                List<ScenarioConditionAvro> conditions = mapConditions(p.getConditionsList());
                List<DeviceActionAvro> actions = mapDeviceActionsFromProto(p.getActionsList());

                payload.setConditions(conditions);
                payload.setActions(actions);

                avro.setPayload(payload);

                log.debug("Mapped SCENARIO_ADDED: name={}, conditions={}, actions={}", payload.getName(), conditions.size(), actions.size());
            }
            case SCENARIO_REMOVED -> {
                var p = request.getScenarioRemoved();
                ScenarioRemovedEventAvro payload = new ScenarioRemovedEventAvro();
                payload.setName(p.getName());
                avro.setPayload(payload);
                log.debug("Mapped SCENARIO_REMOVED: name={}", payload.getName());
            }
            case DEVICE_ACTION_REQUEST -> throw new IllegalArgumentException("DeviceActionRequest is not a hub event for Collector storage");
            case PAYLOAD_NOT_SET -> throw new IllegalArgumentException("Hub payload is not set");
        }

        return avro;
    }

    public HubEventAvro toHubEventAvro(HubEventDto dto) {
        log.debug("Mapping HubEventDto: type={}, hubId={}", dto.getClass().getSimpleName(), dto.getHubId());

        HubEventAvro avro = new HubEventAvro();
        avro.setHubId(dto.getHubId());
        avro.setTimestamp(dto.getTimestamp());

        if (dto instanceof DeviceAddedEventDto e) {
            DeviceAddedEventAvro payload = new DeviceAddedEventAvro();
            payload.setId(e.getId());
            payload.setType(mapDeviceType(e.getDeviceType()));
            avro.setPayload(payload);
            log.debug("Mapped DeviceAddedEventDto: id={}, type={}", payload.getId(), payload.getType());
            return avro;
        }

        if (dto instanceof DeviceRemovedEventDto e) {
            DeviceRemovedEventAvro payload = new DeviceRemovedEventAvro();
            payload.setId(e.getId());
            avro.setPayload(payload);
            log.debug("Mapped DeviceRemovedEventDto: id={}", payload.getId());
            return avro;
        }

        if (dto instanceof ScenarioAddedEventDto e) {
            ScenarioAddedEventAvro payload = new ScenarioAddedEventAvro();
            payload.setName(e.getName());

            log.debug("ScenarioAddedEventDto: name={}, conditions count={}, actions count={}", e.getName(), e.getConditions().size(), e.getActions().size());

            List<ScenarioConditionAvro> conditions = new ArrayList<>();
            for (ScenarioConditionDto c : e.getConditions()) {
                ScenarioConditionAvro cond = new ScenarioConditionAvro();
                cond.setSensorId(c.sensorId());
                cond.setType(mapConditionType(c.type()));
                cond.setOperation(mapConditionOperation(c.operation()));
                cond.setValue(c.value() != null ? c.value() : 0);
                conditions.add(cond);
            }
            payload.setConditions(conditions);

            List<DeviceActionAvro> actions = new ArrayList<>();
            for (DeviceActionDto a : e.getActions()) {
                DeviceActionAvro action = new DeviceActionAvro();
                action.setSensorId(a.sensorId());
                action.setType(mapActionType(a.type()));
                action.setValue(a.value() != null ? a.value() : 0);
                actions.add(action);
            }
            payload.setActions(actions);

            avro.setPayload(payload);

            log.debug("Mapped ScenarioAddedEventDto: name={}, conditions={}, actions={}", payload.getName(), conditions.size(), actions.size());
            return avro;
        }

        if (dto instanceof ScenarioRemovedEventDto e) {
            ScenarioRemovedEventAvro payload = new ScenarioRemovedEventAvro();
            payload.setName(e.getName());
            avro.setPayload(payload);
            log.debug("Mapped ScenarioRemovedEventDto: name={}", payload.getName());
            return avro;
        }

        throw new IllegalArgumentException("Unsupported hub event: " + dto.getClass().getName());
    }

    private List<ScenarioConditionAvro> mapConditions(List<ScenarioConditionProto> conditions) {
        log.debug("Mapping conditions, size={}", conditions.size());

        List<ScenarioConditionAvro> out = new ArrayList<>(conditions.size());

        for (ScenarioConditionProto c : conditions) {
            ScenarioConditionAvro a = new ScenarioConditionAvro();
            a.setSensorId(c.getSensorId());

            ConditionProto cond = c.getCondition();

            a.setType(mapConditionType(cond.getType()));
            a.setOperation(mapConditionOperation(cond.getOperation()));
            a.setValue(mapConditionValueAsInt(cond));

            out.add(a);

            log.debug("Condition mapped: sensorId={}, type={}, operation={}, value={}", a.getSensorId(), a.getType(), a.getOperation(), a.getValue());
        }

        return out;
    }

    private List<DeviceActionAvro> mapDeviceActionsFromProto(List<ScenarioActionProto> actions) {
        log.debug("Mapping actions, size={}", actions.size());

        List<DeviceActionAvro> out = new ArrayList<>(actions.size());

        for (ScenarioActionProto act : actions) {
            DeviceActionAvro a = new DeviceActionAvro();
            a.setSensorId(act.getSensorId());

            DeviceActionProto proto = act.getAction();
            a.setType(mapActionType(proto.getType()));
            a.setValue(proto.getValue());

            out.add(a);

            log.debug("Action mapped: sensorId={}, type={}, value={}", a.getSensorId(), a.getType(), a.getValue());
        }

        return out;
    }

    private int mapConditionValueAsInt(ConditionProto cond) {
        return switch (cond.getValueCase()) {
            case INT_VALUE -> cond.getIntValue();
            case BOOL_VALUE -> cond.getBoolValue() ? 1 : 0;
            case VALUE_NOT_SET -> 0;
        };
    }

    private Instant toInstant(com.google.protobuf.Timestamp ts) {
        return Instant.ofEpochSecond(ts.getSeconds(), ts.getNanos());
    }

    private DeviceTypeAvro mapDeviceType(DeviceTypeProto t) {
        log.debug("Mapping device type proto: {}", t);
        return switch (t) {
            case TEMPERATURE_SENSOR -> DeviceTypeAvro.TEMPERATURE_SENSOR;
            case MOTION_SENSOR -> DeviceTypeAvro.MOTION_SENSOR;
            case LIGHT_SENSOR -> DeviceTypeAvro.LIGHT_SENSOR;
            case SWITCH_SENSOR -> DeviceTypeAvro.SWITCH_SENSOR;
            case CLIMATE_SENSOR -> DeviceTypeAvro.CLIMATE_SENSOR;
            case UNRECOGNIZED -> null;
        };
    }

    private DeviceTypeAvro mapDeviceType(String s) {
        log.debug("Mapping device type string: {}", s);
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
        log.debug("Mapping condition type proto: {}", t);
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

    private ConditionTypeAvro mapConditionType(String t) {
        log.debug("Mapping condition type string: {}", t);
        if (t == null || t.isBlank()) return null;

        return switch (t) {
            case "MOTION" -> ConditionTypeAvro.MOTION;
            case "TEMPERATURE" -> ConditionTypeAvro.TEMPERATURE;
            case "ILLUMINATION" -> ConditionTypeAvro.ILLUMINATION;
            case "HUMIDITY" -> ConditionTypeAvro.HUMIDITY;
            case "SWITCH" -> ConditionTypeAvro.SWITCH;
            case "CO2LEVEL" -> ConditionTypeAvro.CO2LEVEL;
            default -> throw new IllegalArgumentException("Unsupported condition type: " + t);
        };
    }

    private ConditionOperationAvro mapConditionOperation(ConditionOperationProto op) {
        log.debug("Mapping condition operation proto: {}", op);
        return switch (op) {
            case EQUALS -> ConditionOperationAvro.EQUALS;
            case GREATER_THAN -> ConditionOperationAvro.GREATER_THAN;
            case LOWER_THAN -> ConditionOperationAvro.LOWER_THAN;
            case UNRECOGNIZED -> null;
        };
    }

    private ConditionOperationAvro mapConditionOperation(String op) {
        log.debug("Mapping condition operation string: {}", op);
        if (op == null || op.isBlank()) return null;

        return switch (op) {
            case "EQUALS" -> ConditionOperationAvro.EQUALS;
            case "GREATER_THAN" -> ConditionOperationAvro.GREATER_THAN;
            case "LOWER_THAN" -> ConditionOperationAvro.LOWER_THAN;
            default -> throw new IllegalArgumentException("Unsupported condition operation: " + op);
        };
    }

    private ActionTypeAvro mapActionType(ActionTypeProto t) {
        log.debug("Mapping action type proto: {}", t);
        return switch (t) {
            case ACTIVATE -> ActionTypeAvro.ACTIVATE;
            case DEACTIVATE -> ActionTypeAvro.DEACTIVATE;
            case INVERSE -> ActionTypeAvro.INVERSE;
            case SET_VALUE -> ActionTypeAvro.SET_VALUE;
            case UNRECOGNIZED -> null;
        };
    }

    private ActionTypeAvro mapActionType(String t) {
        log.debug("Mapping action type string: {}", t);
        if (t == null || t.isBlank()) return null;

        return switch (t) {
            case "ACTIVATE" -> ActionTypeAvro.ACTIVATE;
            case "DEACTIVATE" -> ActionTypeAvro.DEACTIVATE;
            case "INVERSE" -> ActionTypeAvro.INVERSE;
            case "SET_VALUE" -> ActionTypeAvro.SET_VALUE;
            default -> throw new IllegalArgumentException("Unsupported action type: " + t);
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