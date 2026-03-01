package ru.yandex.practicum.telemetry.analyzer.model;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ScenarioActionId implements Serializable {
    private Long scenarioId;
    private String sensorId;
    private Long actionId;

    public ScenarioActionId() {
    }

    public ScenarioActionId(Long scenarioId, String sensorId, Long actionId) {
        this.scenarioId = scenarioId;
        this.sensorId = sensorId;
        this.actionId = actionId;
    }

    public Long getScenarioId() {
        return scenarioId;
    }

    public String getSensorId() {
        return sensorId;
    }

    public Long getActionId() {
        return actionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ScenarioActionId that)) return false;
        return Objects.equals(scenarioId, that.scenarioId)
                && Objects.equals(sensorId, that.sensorId)
                && Objects.equals(actionId, that.actionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scenarioId, sensorId, actionId);
    }
}