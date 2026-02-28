package ru.yandex.practicum.telemetry.analyzer.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "scenario_actions")
public class ScenarioAction {

    @EmbeddedId
    private ScenarioActionId id;

    @MapsId("scenarioId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "scenario_id")
    private Scenario scenario;

    @MapsId("sensorId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sensor_id")
    private Sensor sensor;

    @MapsId("actionId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "action_id")
    private Action action;

    public ScenarioAction(Scenario scenario, Sensor sensor, Action action) {
        this.scenario = scenario;
        this.sensor = sensor;
        this.action = action;
        this.id = new ScenarioActionId(scenario.getId(), sensor.getId(), action.getId());
    }

    @PrePersist
    void prePersist() {
        if (id == null && scenario != null && sensor != null && action != null) {
            id = new ScenarioActionId(scenario.getId(), sensor.getId(), action.getId());
        }
    }
}