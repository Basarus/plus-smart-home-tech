package ru.yandex.practicum.telemetry.analyzer.model;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ScenarioConditionId implements Serializable {
    private Long scenarioId;
    private String sensorId;
    private Long conditionId;

    public ScenarioConditionId() {
    }

    public ScenarioConditionId(Long scenarioId, String sensorId, Long conditionId) {
        this.scenarioId = scenarioId;
        this.sensorId = sensorId;
        this.conditionId = conditionId;
    }

    public Long getScenarioId() {
        return scenarioId;
    }

    public String getSensorId() {
        return sensorId;
    }

    public Long getConditionId() {
        return conditionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ScenarioConditionId that)) return false;
        return Objects.equals(scenarioId, that.scenarioId)
                && Objects.equals(sensorId, that.sensorId)
                && Objects.equals(conditionId, that.conditionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scenarioId, sensorId, conditionId);
    }
}