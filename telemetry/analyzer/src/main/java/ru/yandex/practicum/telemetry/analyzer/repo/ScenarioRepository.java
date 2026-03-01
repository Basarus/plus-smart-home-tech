package ru.yandex.practicum.telemetry.analyzer.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.yandex.practicum.telemetry.analyzer.model.Scenario;

import java.util.List;
import java.util.Optional;

public interface ScenarioRepository extends JpaRepository<Scenario, Long> {
    List<Scenario> findByHubId(String hubId);
    Optional<Scenario> findByHubIdAndName(String hubId, String name);

    @Query("select distinct s from Scenario s " +
            "left join fetch s.conditions sc " +
            "left join fetch sc.sensor " +
            "left join fetch sc.condition " +
            "left join fetch s.actions sa " +
            "left join fetch sa.sensor " +
            "left join fetch sa.action " +
            "where s.hubId = :hubId")
    List<Scenario> findFullByHubId(String hubId);
}