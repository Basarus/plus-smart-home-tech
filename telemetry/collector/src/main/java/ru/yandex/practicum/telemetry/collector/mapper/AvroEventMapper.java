package ru.yandex.practicum.telemetry.collector.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.telemetry.collector.api.dto.*;

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

        var avro = new SensorEventAvro();
        avro.setId(dto.getId());
        avro.setHubId(dto.getHubId());
        avro.setTimestamp(dto.getTimestamp());
        avro.setPayload(payload);
        return avro;
    }

    public HubEventAvro toAvro(HubEventDto dto) {
        Object payload = switch (dto) {
            case DeviceAddedEventDto e -> deviceAdded(e);
            case DeviceRemovedEventDto e -> deviceRemoved(e);
            case ScenarioAddedEventDto e -> scenarioAdded(e);
            case ScenarioRemovedEventDto e -> scenarioRemoved(e);
            default -> throw new IllegalArgumentException("Unsupported hub event: " + dto.getClass().getName());
        };

        var avro = new HubEventAvro();
        avro.setHubId(dto.getHubId());
        avro.setTimestamp(dto.getTimestamp());
        avro.setPayload(payload);
        return avro;
    }

    private MotionSensorAvro motion(MotionSensorEventDto e) {
        var a = new MotionSensorAvro();
        a.setLinkQuality(e.linkQuality());
        a.setMotion(e.motion());
        a.setVoltage(e.voltage());
        return a;
    }

    private TemperatureSensorAvro temperature(TemperatureSensorEventDto e) {
        var a = new TemperatureSensorAvro();
        a.setTemperatureC(e.temperatureC());
        a.setTemperatureF(e.temperatureF());
        return a;
    }

    private LightSensorAvro light(LightSensorEventDto e) {
        var a = new LightSensorAvro();
        a.setLinkQuality(e.linkQuality());
        a.setLuminosity(e.luminosity());
        return a;
    }

    private ClimateSensorAvro climate(ClimateSensorEventDto e) {
        var a = new ClimateSensorAvro();
        a.setTemperatureC(e.temperatureC());
        a.setHumidity(e.humidity());
        a.setCo2Level(e.co2Level());
        return a;
    }

    private SwitchSensorAvro sw(SwitchSensorEventDto e) {
        var a = new SwitchSensorAvro();
        a.setState(e.state());
        return a;
    }

    private DeviceAddedEventAvro deviceAdded(DeviceAddedEventDto e) {
        var a = new DeviceAddedEventAvro();
        a.setId(e.getId());
        a.setType(mapDeviceType(e.getDeviceType()));
        return a;
    }

    private DeviceRemovedEventAvro deviceRemoved(DeviceRemovedEventDto e) {
        var a = new DeviceRemovedEventAvro();
        a.setId(e.getId());
        return a;
    }

    private ScenarioAddedEventAvro scenarioAdded(ScenarioAddedEventDto e) {
        var a = new ScenarioAddedEventAvro();
        a.setName(e.getName());
        a.setConditions(mapConditions(e.getConditions()));
        a.setActions(mapActions(e.getActions()));
        return a;
    }

    private ScenarioRemovedEventAvro scenarioRemoved(ScenarioRemovedEventDto e) {
        var a = new ScenarioRemovedEventAvro();
        a.setName(e.getName());
        return a;
    }

    private List<ScenarioConditionAvro> mapConditions(List<ScenarioConditionDto> conditions) {
        return conditions.stream().map(c -> {
            var a = new ScenarioConditionAvro();
            a.setSensorId(c.sensorId());
            a.setType(mapConditionType(c.type()));
            a.setOperation(mapOperation(c.operation()));
            a.setValue(c.value());
            return a;
        }).toList();
    }

    private List<DeviceActionAvro> mapActions(List<DeviceActionDto> actions) {
        return actions.stream().map(a0 -> {
            var a = new DeviceActionAvro();
            a.setSensorId(a0.sensorId());
            a.setType(mapActionType(a0.type()));
            a.setValue(a0.value());
            return a;
        }).toList();
    }

    private DeviceTypeAvro mapDeviceType(String v) {
        return switch (v) {
            case "MOTION_SENSOR" -> DeviceTypeAvro.MOTION_SENSOR;
            case "TEMPERATURE_SENSOR" -> DeviceTypeAvro.TEMPERATURE_SENSOR;
            case "LIGHT_SENSOR" -> DeviceTypeAvro.LIGHT_SENSOR;
            case "CLIMATE_SENSOR" -> DeviceTypeAvro.CLIMATE_SENSOR;
            case "SWITCH_SENSOR" -> DeviceTypeAvro.SWITCH_SENSOR;
            default -> throw new IllegalArgumentException("Unknown deviceType: " + v);
        };
    }

    private ConditionTypeAvro mapConditionType(String v) {
        return switch (v) {
            case "MOTION" -> ConditionTypeAvro.MOTION;
            case "LUMINOSITY" -> ConditionTypeAvro.LUMINOSITY;
            case "SWITCH" -> ConditionTypeAvro.SWITCH;
            case "TEMPERATURE" -> ConditionTypeAvro.TEMPERATURE;
            case "CO2LEVEL" -> ConditionTypeAvro.CO2LEVEL;
            case "HUMIDITY" -> ConditionTypeAvro.HUMIDITY;
            default -> throw new IllegalArgumentException("Unknown conditionType: " + v);
        };
    }

    private ConditionOperationAvro mapOperation(String v) {
        return switch (v) {
            case "EQUALS" -> ConditionOperationAvro.EQUALS;
            case "GREATER_THAN" -> ConditionOperationAvro.GREATER_THAN;
            case "LOWER_THAN" -> ConditionOperationAvro.LOWER_THAN;
            default -> throw new IllegalArgumentException("Unknown operation: " + v);
        };
    }

    private ActionTypeAvro mapActionType(String v) {
        return switch (v) {
            case "ACTIVATE" -> ActionTypeAvro.ACTIVATE;
            case "DEACTIVATE" -> ActionTypeAvro.DEACTIVATE;
            case "INVERSE" -> ActionTypeAvro.INVERSE;
            case "SET_VALUE" -> ActionTypeAvro.SET_VALUE;
            default -> throw new IllegalArgumentException("Unknown actionType: " + v);
        };
    }
}