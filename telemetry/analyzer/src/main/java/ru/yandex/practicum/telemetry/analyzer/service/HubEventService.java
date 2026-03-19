package ru.yandex.practicum.telemetry.analyzer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.telemetry.analyzer.model.*;
import ru.yandex.practicum.telemetry.analyzer.repo.*;
import ru.yandex.practicum.telemetry.analyzer.serialization.SpecificAvroDeserializer;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HubEventService {

    private final SpecificAvroDeserializer avroDeserializer;

    private final ScenarioRepository scenarioRepository;
    private final SensorRepository sensorRepository;
    private final ScenarioConditionRepository scenarioConditionRepository;
    private final ScenarioActionRepository scenarioActionRepository;
    private final ConditionRepository conditionRepository;
    private final ActionRepository actionRepository;

    @Transactional
    public void handleHubEvent(byte[] payload) {
        if (payload == null || payload.length == 0) return;

        HubEventAvro event = avroDeserializer.deserialize(payload, HubEventAvro.class);

        String hubId = event.getHubId() == null ? null : event.getHubId().toString();
        if (hubId == null || hubId.isBlank()) return;

        Object p = event.getPayload();
        if (p == null) return;

        if (p instanceof DeviceAddedEventAvro da) {
            handleDeviceAdded(hubId, da);
            return;
        }

        if (p instanceof DeviceRemovedEventAvro dr) {
            handleDeviceRemoved(hubId, dr);
            return;
        }

        if (p instanceof ScenarioAddedEventAvro sa) {
            handleScenarioAdded(hubId, sa);
            return;
        }

        if (p instanceof ScenarioRemovedEventAvro sr) {
            handleScenarioRemoved(hubId, sr);
        }
    }

    private void handleDeviceAdded(String hubId, DeviceAddedEventAvro p) {
        String sensorId = p.getId() == null ? null : p.getId().toString();
        if (sensorId == null || sensorId.isBlank()) return;

        sensorRepository.findByIdAndHubId(sensorId, hubId)
                .orElseGet(() -> sensorRepository.save(new Sensor(sensorId, hubId)));
    }

    private void handleDeviceRemoved(String hubId, DeviceRemovedEventAvro p) {
        String sensorId = p.getId() == null ? null : p.getId().toString();
        if (sensorId == null || sensorId.isBlank()) return;

        sensorRepository.findByIdAndHubId(sensorId, hubId).ifPresent(sensor -> {
            scenarioConditionRepository.deleteByIdSensorId(sensor.getId());
            scenarioActionRepository.deleteByIdSensorId(sensor.getId());
            sensorRepository.delete(sensor);
        });
    }

    private void handleScenarioAdded(String hubId, ScenarioAddedEventAvro p) {
        String name = p.getName() == null ? null : p.getName().toString();
        if (name == null || name.isBlank()) return;

        Scenario scenario = scenarioRepository.findByHubIdAndName(hubId, name)
                .orElseGet(() -> scenarioRepository.save(new Scenario(hubId, name)));

        scenarioConditionRepository.deleteByIdScenarioId(scenario.getId());
        scenarioActionRepository.deleteByIdScenarioId(scenario.getId());

        List<ScenarioConditionAvro> conditions = p.getConditions();
        if (conditions != null) {
            for (ScenarioConditionAvro c : conditions) {
                if (c == null || c.getSensorId() == null || c.getType() == null || c.getOperation() == null) continue;

                String sensorId = c.getSensorId().toString();
                if (sensorId.isBlank()) continue;

                Sensor sensor = sensorRepository.findByIdAndHubId(sensorId, hubId)
                        .orElseGet(() -> sensorRepository.save(new Sensor(sensorId, hubId)));

                Integer value = toIntValue(c.getValue());
                if (value == null) continue;

                Condition cond = conditionRepository.save(new Condition(
                        c.getType().name(),
                        c.getOperation().name(),
                        value
                ));

                scenarioConditionRepository.save(new ScenarioCondition(scenario, sensor, cond));
            }
        }

        List<DeviceActionAvro> actions = p.getActions();
        if (actions != null) {
            for (DeviceActionAvro a : actions) {
                if (a == null || a.getSensorId() == null || a.getType() == null) continue;

                String targetId = a.getSensorId().toString();
                if (targetId.isBlank()) continue;

                Sensor sensor = sensorRepository.findByIdAndHubId(targetId, hubId)
                        .orElseGet(() -> sensorRepository.save(new Sensor(targetId, hubId)));

                Integer value = toIntValue(a.getValue());
                if (value == null) value = 0;

                Action action = actionRepository.save(new Action(
                        a.getType().name(),
                        value
                ));

                scenarioActionRepository.save(new ScenarioAction(scenario, sensor, action));
            }
        }
    }

    private void handleScenarioRemoved(String hubId, ScenarioRemovedEventAvro p) {
        String name = p.getName() == null ? null : p.getName().toString();
        if (name == null || name.isBlank()) return;

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
        if (value instanceof Number n) return n.intValue();
        if (value instanceof CharSequence cs) {
            try {
                return Integer.parseInt(cs.toString());
            } catch (Exception ignored) {
                return null;
            }
        }
        return null;
    }
}