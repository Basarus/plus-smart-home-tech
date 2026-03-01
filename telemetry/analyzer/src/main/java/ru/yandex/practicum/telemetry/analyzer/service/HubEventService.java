package ru.yandex.practicum.telemetry.analyzer.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.telemetry.analyzer.model.*;
import ru.yandex.practicum.telemetry.analyzer.repo.*;
import ru.yandex.practicum.telemetry.analyzer.serialization.SpecificAvroDeserializer;

import java.util.List;
import java.util.Optional;

@Service
public class HubEventService {
    private final ScenarioRepository scenarioRepository;
    private final SensorRepository sensorRepository;
    private final ConditionRepository conditionRepository;
    private final ActionRepository actionRepository;
    private final ScenarioConditionRepository scenarioConditionRepository;
    private final ScenarioActionRepository scenarioActionRepository;
    private final SpecificAvroDeserializer avroDeserializer;

    public HubEventService(ScenarioRepository scenarioRepository,
                           SensorRepository sensorRepository,
                           ConditionRepository conditionRepository,
                           ActionRepository actionRepository,
                           ScenarioConditionRepository scenarioConditionRepository,
                           ScenarioActionRepository scenarioActionRepository,
                           SpecificAvroDeserializer avroDeserializer) {
        this.scenarioRepository = scenarioRepository;
        this.sensorRepository = sensorRepository;
        this.conditionRepository = conditionRepository;
        this.actionRepository = actionRepository;
        this.scenarioConditionRepository = scenarioConditionRepository;
        this.scenarioActionRepository = scenarioActionRepository;
        this.avroDeserializer = avroDeserializer;
    }

    @Transactional
    public void handle(byte[] payload) {
        HubEventAvro event = avroDeserializer.deserialize(payload, HubEventAvro.class);
        Object eventPayload = event.getPayload();
        if (eventPayload instanceof DeviceAddedEventAvro p) {
            handleDeviceAdded(event.getHubId().toString(), p);
            return;
        }
        if (eventPayload instanceof DeviceRemovedEventAvro p) {
            handleDeviceRemoved(event.getHubId().toString(), p);
            return;
        }
        if (eventPayload instanceof ScenarioAddedEventAvro p) {
            handleScenarioAdded(event.getHubId().toString(), p);
            return;
        }
        if (eventPayload instanceof ScenarioRemovedEventAvro p) {
            handleScenarioRemoved(event.getHubId().toString(), p);
        }
    }

    private void handleDeviceAdded(String hubId, DeviceAddedEventAvro p) {
        String sensorId = p.getId().toString();
        Optional<Sensor> existing = sensorRepository.findByIdAndHubId(sensorId, hubId);
        if (existing.isPresent()) {
            return;
        }
        sensorRepository.save(new Sensor(sensorId, hubId));
    }

    private void handleDeviceRemoved(String hubId, DeviceRemovedEventAvro p) {
        String sensorId = p.getId().toString();
        Optional<Sensor> existing = sensorRepository.findByIdAndHubId(sensorId, hubId);
        if (existing.isEmpty()) {
            return;
        }
        scenarioConditionRepository.deleteByIdSensorId(sensorId);
        scenarioActionRepository.deleteByIdSensorId(sensorId);
        sensorRepository.deleteById(sensorId);
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

                Condition cond = conditionRepository.save(new Condition(
                        String.valueOf(c.getType()),
                        String.valueOf(c.getOperation()),
                        c.getValue()
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

                Action act = actionRepository.save(new Action(String.valueOf(a.getType()), a.getValue()));
                scenarioActionRepository.save(new ScenarioAction(scenario, sensor, act));
            }
        }
    }

    private void handleScenarioRemoved(String hubId, ScenarioRemovedEventAvro p) {
        String name = p.getName().toString();
        Optional<Scenario> scenarioOpt = scenarioRepository.findByHubIdAndName(hubId, name);
        if (scenarioOpt.isEmpty()) {
            return;
        }
        Scenario scenario = scenarioOpt.get();
        scenarioConditionRepository.deleteByIdScenarioId(scenario.getId());
        scenarioActionRepository.deleteByIdScenarioId(scenario.getId());
        scenarioRepository.delete(scenario);
    }
}