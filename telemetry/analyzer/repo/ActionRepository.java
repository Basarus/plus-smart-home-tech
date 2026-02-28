package ru.yandex.practicum.telemetry.analyzer.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.telemetry.analyzer.model.Action;

public interface ActionRepository extends JpaRepository<Action, Long> {
}