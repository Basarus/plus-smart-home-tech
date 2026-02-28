package ru.yandex.practicum.telemetry.analyzer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.kafka.telemetry.event.ActionTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.ConditionOperationAvro;
import ru.yandex.practicum.kafka.telemetry.event.ConditionTypeAvro;
import ru.yandex.practicum.telemetry.analyzer.model.Scenario;
import ru.yandex.practicum.telemetry.analyzer.model.ScenarioAction;
import ru.yandex.practicum.telemetry.analyzer.model.ScenarioCondition;
import ru.yandex.practicum.telemetry.analyzer.repo.ScenarioActionLinkRepository;
import ru.yandex.practicum.telemetry.analyzer.repo.ScenarioActionRepository;
import ru.yandex.practicum.telemetry.analyzer.repo.ScenarioConditionLinkRepository;
import ru.yandex.practicum.telemetry.analyzer.repo.ScenarioConditionRepository;
import ru.yandex.practicum.telemetry.analyzer.repo.ScenarioRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ScenarioRuntimeLoader {

    private final ScenarioRepository scenarioRepository;
    private final ScenarioConditionLinkRepository scenarioConditionLinkRepository;
    private final ScenarioActionLinkRepository scenarioActionLinkRepository;
    private final ScenarioActionRepository scenarioActionRepository;
    private final ScenarioConditionRepository scenarioConditionRepository;

    @Transactional(readOnly = true)
    public List<ScenarioRuntime> loadByHubId(String hubId) {
        List<Scenario> scenarios = scenarioRepository.findAllByHubId(hubId);
        if (scenarios.isEmpty()) return List.of();

        List<Long> scenarioIds = scenarios.stream().map(Scenario::getId).toList();

        List<ScenarioCondition> condLinks = scenarioConditionRepository.findAllByScenarioIds(scenarioIds);
        List<ScenarioAction> actionLinks = scenarioActionRepository.findAllByScenarioIds(scenarioIds);

        Map<Long, List<ScenarioCondition>> condByScenario = condLinks.stream()
                .collect(Collectors.groupingBy(sc -> sc.getScenario().getId()));

        Map<Long, List<ScenarioAction>> actionByScenario = actionLinks.stream()
                .collect(Collectors.groupingBy(sa -> sa.getScenario().getId()));

        List<ScenarioRuntime> result = new ArrayList<>(scenarios.size());

        for (Scenario s : scenarios) {
            List<ScenarioRuntime.ConditionCheck> conditions = condByScenario
                    .getOrDefault(s.getId(), List.of())
                    .stream()
                    .map(link -> new ScenarioRuntime.ConditionCheck(
                            link.getSensor().getId(),
                            link.getCondition().getType(),
                            link.getCondition().getOperation(),
                            link.getCondition().getValue()
                    ))
                    .toList();

            List<ScenarioRuntime.ActionCmd> actions = actionByScenario
                    .getOrDefault(s.getId(), List.of())
                    .stream()
                    .map(link -> new ScenarioRuntime.ActionCmd(
                            link.getSensor().getId(),
                            toActionType(link.getAction().getType()),
                            link.getAction().getValue()
                    ))
                    .toList();

            result.add(new ScenarioRuntime(
                    s.getId(),
                    s.getHubId(),
                    s.getName(),
                    conditions,
                    actions
            ));
        }

        return result;
    }

    private ScenarioRuntime toRuntime(Scenario scenario) {
        var conditionLinks = scenarioConditionLinkRepository.findAllByScenarioIdWithRefs(scenario.getId());
        var actionLinks = scenarioActionLinkRepository.findAllByScenarioIdWithRefs(scenario.getId());

        List<ScenarioRuntime.ConditionCheck> conditions = conditionLinks.stream()
                .map(l -> new ScenarioRuntime.ConditionCheck(
                        l.getSensor().getId(),
                        l.getCondition().getType(),
                        l.getCondition().getOperation(),
                        l.getCondition().getValue()
                ))
                .toList();

        List<ScenarioRuntime.ActionCmd> actions = actionLinks.stream()
                .map(l -> new ScenarioRuntime.ActionCmd(
                        l.getSensor().getId(),
                        toActionType(l.getAction().getType()),
                        l.getAction().getValue()
                ))
                .toList();

        return new ScenarioRuntime(
                scenario.getId(),
                scenario.getHubId(),
                scenario.getName(),
                conditions,
                actions
        );
    }

    private ActionTypeAvro toActionType(String dbValue) {
        if (dbValue == null || dbValue.isBlank()) return null;
        return ActionTypeAvro.valueOf(dbValue);
    }

    private ConditionOperationAvro toConditionOperation(String dbValue) {
        if (dbValue == null || dbValue.isBlank()) return null;
        return ConditionOperationAvro.valueOf(dbValue);
    }
}