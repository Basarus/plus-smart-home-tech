package ru.yandex.practicum.telemetry.analyzer.model;

import jakarta.persistence.*;

@Entity
@Table(name = "scenario_conditions")
public class ScenarioCondition {
    @EmbeddedId
    private ScenarioConditionId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scenario_id", insertable = false, updatable = false)
    private Scenario scenario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sensor_id", insertable = false, updatable = false)
    private Sensor sensor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "condition_id", insertable = false, updatable = false)
    private Condition condition;

    public ScenarioCondition() {
    }

    public ScenarioCondition(Scenario scenario, Sensor sensor, Condition condition) {
        this.id = new ScenarioConditionId(scenario.getId(), sensor.getId(), condition.getId());
        this.scenario = scenario;
        this.sensor = sensor;
        this.condition = condition;
    }

    public ScenarioConditionId getId() {
        return id;
    }

    public Scenario getScenario() {
        return scenario;
    }

    public Sensor getSensor() {
        return sensor;
    }

    public Condition getCondition() {
        return condition;
    }
}