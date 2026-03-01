package ru.yandex.practicum.telemetry.analyzer.service;

import java.util.List;

public record ScenarioRuntime(
        List<ConditionCheck> conditions,
        List<ActionCmd> actions
) {
    public record ConditionCheck(String sensorId, String type, String operation, Integer value) {
    }

    public record ActionCmd(String sensorId, String type, Integer value) {
    }
}