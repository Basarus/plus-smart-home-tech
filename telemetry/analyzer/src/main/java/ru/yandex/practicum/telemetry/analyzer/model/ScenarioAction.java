package ru.yandex.practicum.telemetry.analyzer.model;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "scenario_actions")
public class ScenarioAction {

    @EmbeddedId
    private ScenarioActionId id = new ScenarioActionId();

    @MapsId("scenarioId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scenario_id")
    private Scenario scenario;

    @MapsId("sensorId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sensor_id")
    private Sensor sensor;

    @MapsId("actionId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_id")
    private Action action;

    public ScenarioAction() {
    }

    public ScenarioAction(Scenario scenario, Sensor sensor, Action action) {
        this.scenario = scenario;
        this.sensor = sensor;
        this.action = action;
    }

    public ScenarioActionId getId() {
        return id;
    }

    public Scenario getScenario() {
        return scenario;
    }

    public Sensor getSensor() {
        return sensor;
    }

    public Action getAction() {
        return action;
    }
}