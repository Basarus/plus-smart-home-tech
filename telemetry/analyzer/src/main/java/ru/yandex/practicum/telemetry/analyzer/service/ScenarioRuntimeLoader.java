package ru.yandex.practicum.telemetry.analyzer.service;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.telemetry.analyzer.model.Scenario;
import ru.yandex.practicum.telemetry.analyzer.model.ScenarioAction;
import ru.yandex.practicum.telemetry.analyzer.model.ScenarioCondition;
import ru.yandex.practicum.telemetry.analyzer.repo.ScenarioRepository;

import java.util.List;
import java.util.Objects;

@Component
public class ScenarioRuntimeLoader {

    private final ScenarioRepository scenarioRepository;

    public ScenarioRuntimeLoader(ScenarioRepository scenarioRepository) {
        this.scenarioRepository = scenarioRepository;
    }

    @Transactional(readOnly = true)
    public List<ScenarioRuntime> loadByHubId(String hubId) {
        List<Scenario> scenarios = scenarioRepository.findFullByHubId(hubId);
        return scenarios.stream().map(this::mapScenario).toList();
    }

    private ScenarioRuntime mapScenario(Scenario s) {
        List<ScenarioRuntime.ConditionCheck> conditions = s.getConditions().stream()
                .map(this::mapCondition)
                .filter(Objects::nonNull)
                .toList();

        List<ScenarioRuntime.ActionCmd> actions = s.getActions().stream()
                .map(this::mapAction)
                .filter(Objects::nonNull)
                .toList();

        return new ScenarioRuntime(s.getHubId(), s.getName(), conditions, actions);
    }

    private ScenarioRuntime.ConditionCheck mapCondition(ScenarioCondition sc) {
        if (sc.getSensor() == null || sc.getCondition() == null) return null;
        if (sc.getCondition().getValue() == null) return null;
        return new ScenarioRuntime.ConditionCheck(
                sc.getSensor().getId(),
                sc.getCondition().getType(),
                sc.getCondition().getOperation(),
                sc.getCondition().getValue()
        );
    }

    private ScenarioRuntime.ActionCmd mapAction(ScenarioAction sa) {
        if (sa.getSensor() == null || sa.getAction() == null) return null;
        if (sa.getAction().getValue() == null) return null;
        return new ScenarioRuntime.ActionCmd(
                sa.getSensor().getId(),
                sa.getAction().getType(),
                sa.getAction().getValue()
        );
    }
}