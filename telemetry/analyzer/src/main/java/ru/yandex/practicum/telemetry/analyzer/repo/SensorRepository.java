package ru.yandex.practicum.telemetry.analyzer.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.telemetry.analyzer.model.Sensor;

import java.util.Optional;

public interface SensorRepository extends JpaRepository<Sensor, String> {

    Optional<Sensor> findByIdAndHubId(String id, String hubId);
}