package ru.yandex.practicum.telemetry.analyzer.model;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "scenario_conditions")
public class ScenarioCondition {

    @EmbeddedId
    private ScenarioConditionId id = new ScenarioConditionId();

    @MapsId("scenarioId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scenario_id")
    private Scenario scenario;

    @MapsId("sensorId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sensor_id")
    private Sensor sensor;

    @MapsId("conditionId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "condition_id")
    private Condition condition;

    public ScenarioCondition() {
    }

    public ScenarioCondition(Scenario scenario, Sensor sensor, Condition condition) {
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