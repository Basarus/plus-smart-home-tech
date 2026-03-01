package ru.yandex.practicum.telemetry.analyzer.model;

import jakarta.persistence.*;

@Entity
@Table(name = "scenario_actions")
public class ScenarioAction {
    @EmbeddedId
    private ScenarioActionId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scenario_id", insertable = false, updatable = false)
    private Scenario scenario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sensor_id", insertable = false, updatable = false)
    private Sensor sensor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_id", insertable = false, updatable = false)
    private Action action;

    public ScenarioAction() {
    }

    public ScenarioAction(Scenario scenario, Sensor sensor, Action action) {
        this.id = new ScenarioActionId(scenario.getId(), sensor.getId(), action.getId());
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