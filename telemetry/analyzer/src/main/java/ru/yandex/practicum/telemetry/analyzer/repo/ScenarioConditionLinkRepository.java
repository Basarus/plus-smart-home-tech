package ru.yandex.practicum.telemetry.analyzer.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yandex.practicum.telemetry.analyzer.model.ScenarioConditionId;
import ru.yandex.practicum.telemetry.analyzer.model.ScenarioConditionLink;

import java.util.List;

public interface ScenarioConditionLinkRepository extends JpaRepository<ScenarioConditionLink, ScenarioConditionId> {

    @Query("""
           select scl
           from ScenarioConditionLink scl
           join fetch scl.sensor s
           join fetch scl.condition c
           where scl.scenario.id = :scenarioId
           """)
    List<ScenarioConditionLink> findAllByScenarioIdWithRefs(@Param("scenarioId") Long scenarioId);
}