package ru.yandex.practicum.telemetry.analyzer.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yandex.practicum.telemetry.analyzer.model.ScenarioActionId;
import ru.yandex.practicum.telemetry.analyzer.model.ScenarioActionLink;

import java.util.List;

public interface ScenarioActionLinkRepository extends JpaRepository<ScenarioActionLink, ScenarioActionId> {

    @Query("""
           select sal
           from ScenarioActionLink sal
           join fetch sal.sensor s
           join fetch sal.action a
           where sal.scenario.id = :scenarioId
           """)
    List<ScenarioActionLink> findAllByScenarioIdWithRefs(@Param("scenarioId") Long scenarioId);
}