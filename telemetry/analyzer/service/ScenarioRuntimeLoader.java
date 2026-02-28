package ru.yandex.practicum.telemetry.analyzer.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.telemetry.analyzer.model.Scenario;

import java.util.ArrayList;
import java.util.List;

@Component
public class ScenarioRuntimeLoader {
    private static EntityManager em;

    @PersistenceContext
    public void setEm(EntityManager em) {
        ScenarioRuntimeLoader.em = em;
    }

    @Transactional(readOnly = true)
    public static ScenarioRuntime loadScenarioRuntime(Scenario scenario) {
        if (scenario == null || scenario.getId() == null) {
            return null;
        }

        List<Object[]> condRows = em.createQuery("""
                select sc.sensor.id, sc.condition.type, sc.condition.operation, sc.condition.value
                from ScenarioCondition sc
                where sc.scenario.id = :sid
                """, Object[].class)
                .setParameter("sid", scenario.getId())
                .getResultList();

        List<Object[]> actRows = em.createQuery("""
                select sa.sensor.id, sa.action.type, sa.action.value
                from ScenarioAction sa
                where sa.scenario.id = :sid
                """, Object[].class)
                .setParameter("sid", scenario.getId())
                .getResultList();

        List<ScenarioRuntime.ConditionCheck> conditions = new ArrayList<>();
        for (Object[] r : condRows) {
            conditions.add(new ScenarioRuntime.ConditionCheck(
                    String.valueOf(r[0]),
                    r[1] == null ? null : String.valueOf(r[1]),
                    r[2] == null ? null : String.valueOf(r[2]),
                    (Integer) r[3]
            ));
        }

        List<ScenarioRuntime.ActionCmd> actions = new ArrayList<>();
        for (Object[] r : actRows) {
            actions.add(new ScenarioRuntime.ActionCmd(
                    String.valueOf(r[0]),
                    r[1] == null ? null : String.valueOf(r[1]),
                    (Integer) r[2]
            ));
        }

        return new ScenarioRuntime(conditions, actions);
    }
}