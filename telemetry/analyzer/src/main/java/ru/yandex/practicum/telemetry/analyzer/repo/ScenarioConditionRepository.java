package ru.yandex.practicum.telemetry.analyzer.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yandex.practicum.telemetry.analyzer.model.ScenarioCondition;
import ru.yandex.practicum.telemetry.analyzer.model.ScenarioConditionId;

import java.util.List;

public interface ScenarioConditionRepository extends JpaRepository<ScenarioCondition, ScenarioConditionId> {

    @Modifying
    @Query("delete from ScenarioCondition sc where sc.id.scenarioId = :scenarioId")
    void deleteByIdScenarioId(@Param("scenarioId") Long scenarioId);

    @Modifying
    @Query("delete from ScenarioCondition sc where sc.id.sensorId = :sensorId")
    void deleteByIdSensorId(@Param("sensorId") String sensorId);

    @Query("""
              select sc from ScenarioCondition sc
              join fetch sc.scenario s
              join fetch sc.sensor
              join fetch sc.condition
              where s.id in :scenarioIds
            """)
    List<ScenarioCondition> findAllByScenarioIds(@Param("scenarioIds") List<Long> scenarioIds);

}