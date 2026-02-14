package ru.yandex.practicum.telemetry.collector.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.telemetry.collector.api.dto.*;

import java.util.List;

@Component
public class AvroEventMapper {
    public SensorEventAvro toAvro(SensorEventDto dto) {
        Object payload = switch (dto) {
            case MotionSensorEventDto e -> new MotionSensorAvro(e.getLinkQuality(), e.getMotion(), e.getVoltage());
            case TemperatureSensorEventDto e -> new TemperatureSensorAvro(e.getTemperatureC(), e.getTemperatureF());
            case LightSensorEventDto e -> new LightSensorAvro(e.getLinkQuality(), e.getLuminosity());
            case ClimateSensorEventDto e -> new ClimateSensorAvro(e.getTemperatureC(), e.getHumidity(), e.getCo2Level());
            case SwitchSensorEventDto e -> new SwitchSensorAvro(e.getState());
            default -> throw new IllegalArgumentException("Unsupported sensor event");
        };
        return new SensorEventAvro(dto.getId(), dto.getHubId(), dto.getTimestamp().toEpochMilli(), payload);
    }

    public HubEventAvro toAvro(HubEventDto dto) {
        Object payload = switch (dto) {
            case DeviceAddedEventDto e -> new DeviceAddedEventAvro(e.getId(), DeviceTypeAvro.valueOf(e.getDeviceType()));
            case DeviceRemovedEventDto e -> new DeviceRemovedEventAvro(e.getId());
            case ScenarioAddedEventDto e -> new ScenarioAddedEventAvro(
                    e.getName(),
                    mapConditions(e.getConditions()),
                    mapActions(e.getActions())
            );
            case ScenarioRemovedEventDto e -> new ScenarioRemovedEventAvro(e.getName());
            default -> throw new IllegalArgumentException("Unsupported hub event");
        };
        return new HubEventAvro(dto.getHubId(), dto.getTimestamp().toEpochMilli(), payload);
    }

    private List<ScenarioConditionAvro> mapConditions(List<ScenarioConditionDto> conditions) {
        return conditions.stream()
                .map(c -> new ScenarioConditionAvro(
                        c.getSensorId(),
                        ConditionTypeAvro.valueOf(c.getType()),
                        ConditionOperationAvro.valueOf(c.getOperation()),
                        c.getValue()
                ))
                .toList();
    }

    private List<DeviceActionAvro> mapActions(List<DeviceActionDto> actions) {
        return actions.stream()
                .map(a -> new DeviceActionAvro(
                        a.getSensorId(),
                        ActionTypeAvro.valueOf(a.getType()),
                        a.getValue()
                ))
                .toList();
    }
}