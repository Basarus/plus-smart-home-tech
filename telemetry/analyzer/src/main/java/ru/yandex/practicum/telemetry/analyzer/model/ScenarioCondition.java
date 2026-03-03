package ru.yandex.practicum.telemetry.analyzer.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "scenario_conditions")
public class ScenarioCondition {

    @EmbeddedId
    private ScenarioConditionId id;

    @MapsId("scenarioId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "scenario_id")
    private Scenario scenario;

    @MapsId("sensorId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sensor_id")
    private Sensor sensor;

    @MapsId("conditionId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "condition_id")
    private Condition condition;

    public ScenarioCondition(Scenario scenario, Sensor sensor, Condition condition) {
        this.scenario = scenario;
        this.sensor = sensor;
        this.condition = condition;
        this.id = new ScenarioConditionId(scenario.getId(), sensor.getId(), condition.getId());
    }

    @PrePersist
    void prePersist() {
        if (id == null && scenario != null && sensor != null && condition != null) {
            id = new ScenarioConditionId(scenario.getId(), sensor.getId(), condition.getId());
        }
    }
}