package ru.yandex.practicum.telemetry.analyzer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.telemetry.analyzer.model.*;
import ru.yandex.practicum.telemetry.analyzer.repo.*;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HubEventHandler {

    private final ScenarioRepository scenarioRepository;
    private final SensorRepository sensorRepository;
    private final ConditionRepository conditionRepository;
    private final ActionRepository actionRepository;
    private final ScenarioConditionRepository scenarioConditionRepository;
    private final ScenarioActionRepository scenarioActionRepository;

    public void handleHubEvent(HubEventAvro event) {
        String hubId = event.getHubId() == null ? null : event.getHubId().toString();
        if (hubId == null || hubId.isBlank()) return;

        Object payload = event.getPayload();
        if (payload instanceof DeviceAddedEventAvro p) {
            handleDeviceAdded(hubId, p);
            return;
        }
        if (payload instanceof DeviceRemovedEventAvro p) {
            handleDeviceRemoved(hubId, p);
            return;
        }
        if (payload instanceof ScenarioAddedEventAvro p) {
            handleScenarioAdded(hubId, p);
            return;
        }
        if (payload instanceof ScenarioRemovedEventAvro p) {
            handleScenarioRemoved(hubId, p);
        }
    }

    private void handleDeviceAdded(String hubId, DeviceAddedEventAvro p) {
        String sensorId = p.getId().toString();
        sensorRepository.findByIdAndHubId(sensorId, hubId)
                .orElseGet(() -> sensorRepository.save(new Sensor(sensorId, hubId)));
    }

    private void handleDeviceRemoved(String hubId, DeviceRemovedEventAvro p) {
        String sensorId = p.getId().toString();
        sensorRepository.findByIdAndHubId(sensorId, hubId).ifPresent(sensor -> {
            scenarioConditionRepository.deleteByIdSensorId(sensor.getId());
            scenarioActionRepository.deleteByIdSensorId(sensor.getId());
            sensorRepository.delete(sensor);
        });
    }

    private void handleScenarioAdded(String hubId, ScenarioAddedEventAvro p) {
        String name = p.getName().toString();
        Scenario scenario = scenarioRepository.findByHubIdAndName(hubId, name)
                .orElseGet(() -> scenarioRepository.save(new Scenario(hubId, name)));

        scenarioConditionRepository.deleteByIdScenarioId(scenario.getId());
        scenarioActionRepository.deleteByIdScenarioId(scenario.getId());

        List<ScenarioConditionAvro> conditions = p.getConditions();
        if (conditions != null) {
            for (ScenarioConditionAvro c : conditions) {
                String sensorId = c.getSensorId().toString();
                Sensor sensor = sensorRepository.findByIdAndHubId(sensorId, hubId)
                        .orElseGet(() -> sensorRepository.save(new Sensor(sensorId, hubId)));

                Integer value = toIntValue(c.getValue());

                Condition cond = conditionRepository.save(new Condition(
                        String.valueOf(c.getType()),
                        String.valueOf(c.getOperation()),
                        value
                ));

                scenarioConditionRepository.save(new ScenarioCondition(scenario, sensor, cond));
            }
        }

        List<DeviceActionAvro> actions = p.getActions();
        if (actions != null) {
            for (DeviceActionAvro a : actions) {
                String sensorId = a.getSensorId().toString();
                Sensor sensor = sensorRepository.findByIdAndHubId(sensorId, hubId)
                        .orElseGet(() -> sensorRepository.save(new Sensor(sensorId, hubId)));

                Integer value = toIntValue(a.getValue());

                Action action = actionRepository.save(new Action(
                        String.valueOf(a.getType()),
                        value
                ));

                scenarioActionRepository.save(new ScenarioAction(scenario, sensor, action));
            }
        }
    }

    private void handleScenarioRemoved(String hubId, ScenarioRemovedEventAvro p) {
        String name = p.getName().toString();
        scenarioRepository.findByHubIdAndName(hubId, name).ifPresent(scenario -> {
            scenarioConditionRepository.deleteByIdScenarioId(scenario.getId());
            scenarioActionRepository.deleteByIdScenarioId(scenario.getId());
            scenarioRepository.delete(scenario);
        });
    }

    private Integer toIntValue(Object value) {
        if (value == null) return null;
        if (value instanceof Integer i) return i;
        if (value instanceof Long l) return Math.toIntExact(l);
        if (value instanceof Boolean b) return b ? 1 : 0;
        if (value instanceof CharSequence cs) return Integer.parseInt(cs.toString());
        return null;
    }
}