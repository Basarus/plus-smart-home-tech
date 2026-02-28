package ru.yandex.practicum.telemetry.analyzer.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.telemetry.analyzer.model.Action;
import ru.yandex.practicum.telemetry.analyzer.model.Condition;
import ru.yandex.practicum.telemetry.analyzer.model.Scenario;
import ru.yandex.practicum.telemetry.analyzer.model.ScenarioAction;
import ru.yandex.practicum.telemetry.analyzer.model.ScenarioCondition;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class ScenarioRuntimeLoader {

    @PersistenceContext
    private EntityManager em;

    @Transactional(readOnly = true)
    public List<ScenarioRuntime> loadScenarioRuntime(String hubId) {
        List<Scenario> scenarios = em.createQuery(
                        "select s from Scenario s where s.hubId = :hubId",
                        Scenario.class
                )
                .setParameter("hubId", hubId)
                .getResultList();

        return scenarios.stream()
                .map(this::mapScenario)
                .collect(Collectors.toList());
    }

    private ScenarioRuntime mapScenario(Scenario s) {
        List<ScenarioConditionRuntime> conditions = s.getConditions().stream()
                .map(this::mapCondition)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        List<ScenarioActionRuntime> actions = s.getActions().stream()
                .map(this::mapAction)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return new ScenarioRuntime(s.getHubId(), s.getName(), conditions, actions);
    }

    private ScenarioConditionRuntime mapCondition(ScenarioCondition sc) {
        if (sc == null || sc.getSensor() == null || sc.getCondition() == null) return null;
        Condition c = sc.getCondition();
        return new ScenarioConditionRuntime(
                sc.getSensor().getId(),
                c.getType(),
                c.getOperation(),
                c.getValue()
        );
    }

    private ScenarioActionRuntime mapAction(ScenarioAction sa) {
        if (sa == null || sa.getSensor() == null || sa.getAction() == null) return null;
        Action a = sa.getAction();
        return new ScenarioActionRuntime(
                sa.getSensor().getId(),
                a.getType(),
                a.getValue()
        );
    }
}