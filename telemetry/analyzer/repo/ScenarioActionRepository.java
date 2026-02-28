package ru.yandex.practicum.telemetry.analyzer.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.telemetry.analyzer.model.ScenarioAction;
import ru.yandex.practicum.telemetry.analyzer.model.ScenarioActionId;

public interface ScenarioActionRepository extends JpaRepository<ScenarioAction, ScenarioActionId> {
    @Transactional
    void deleteByIdScenarioId(Long scenarioId);

    @Transactional
    void deleteByIdSensorId(String sensorId);
}