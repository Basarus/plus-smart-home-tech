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
            default -> throw new IllegalArgumentException("Unsupported sensor event");
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
            default -> throw new IllegalArgumentException("Unsupported hub event");
        };

        var avro = new HubEventAvro();
        avro.setHubId(dto.getHubId());
        avro.setTimestamp(dto.getTimestamp());
        avro.setPayload(payload);
        return avro;
    }

    private MotionSensorAvro motion(MotionSensorEventDto e) {
        var a = new MotionSensorAvro();
        a.setLinkQuality(e.getLinkQuality());
        a.setMotion(e.getMotion());
        a.setVoltage(e.getVoltage());
        return a;
    }

    private TemperatureSensorAvro temperature(TemperatureSensorEventDto e) {
        var a = new TemperatureSensorAvro();
        a.setTemperatureC(e.getTemperatureC());
        a.setTemperatureF(e.getTemperatureF());
        return a;
    }

    private LightSensorAvro light(LightSensorEventDto e) {
        var a = new LightSensorAvro();
        a.setLinkQuality(e.getLinkQuality());
        a.setLuminosity(e.getLuminosity());
        return a;
    }

    private ClimateSensorAvro climate(ClimateSensorEventDto e) {
        var a = new ClimateSensorAvro();
        a.setTemperatureC(e.getTemperatureC());
        a.setHumidity(e.getHumidity());
        a.setCo2Level(e.getCo2Level());
        return a;
    }

    private SwitchSensorAvro sw(SwitchSensorEventDto e) {
        var a = new SwitchSensorAvro();
        a.setState(e.getState());
        return a;
    }

    private DeviceAddedEventAvro deviceAdded(DeviceAddedEventDto e) {
        var a = new DeviceAddedEventAvro();
        a.setId(e.getId());
        a.setType(DeviceTypeAvro.valueOf(e.getDeviceType()));
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
            a.setSensorId(c.getSensorId());
            a.setType(ConditionTypeAvro.valueOf(c.getType()));
            a.setOperation(ConditionOperationAvro.valueOf(c.getOperation()));
            a.setValue(c.getValue());
            return a;
        }).toList();
    }

    private List<DeviceActionAvro> mapActions(List<DeviceActionDto> actions) {
        return actions.stream().map(a0 -> {
            var a = new DeviceActionAvro();
            a.setSensorId(a0.getSensorId());
            a.setType(ActionTypeAvro.valueOf(a0.getType()));
            a.setValue(a0.getValue());
            return a;
        }).toList();
    }
}