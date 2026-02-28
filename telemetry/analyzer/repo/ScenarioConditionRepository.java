package ru.yandex.practicum.telemetry.analyzer.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.telemetry.analyzer.model.ScenarioCondition;
import ru.yandex.practicum.telemetry.analyzer.model.ScenarioConditionId;

public interface ScenarioConditionRepository extends JpaRepository<ScenarioCondition, ScenarioConditionId> {
    @Transactional
    void deleteByIdScenarioId(Long scenarioId);

    @Transactional
    void deleteByIdSensorId(String sensorId);
}