package ru.yandex.practicum.telemetry.analyzer.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yandex.practicum.telemetry.analyzer.model.ScenarioAction;
import ru.yandex.practicum.telemetry.analyzer.model.ScenarioActionId;

import java.util.List;

public interface ScenarioActionRepository extends JpaRepository<ScenarioAction, ScenarioActionId> {

    @Modifying
    @Query("delete from ScenarioAction sa where sa.id.scenarioId = :scenarioId")
    void deleteByIdScenarioId(@Param("scenarioId") Long scenarioId);

    @Modifying
    @Query("delete from ScenarioAction sa where sa.id.sensorId = :sensorId")
    void deleteByIdSensorId(@Param("sensorId") String sensorId);

    @Query("""
                select sa from ScenarioAction sa
                join fetch sa.sensor
                join fetch sa.action
                join fetch sa.scenario s
                where s.id in :scenarioIds
            """)
    List<ScenarioAction> findAllByScenarioIds(@Param("scenarioIds") List<Long> scenarioIds);
}