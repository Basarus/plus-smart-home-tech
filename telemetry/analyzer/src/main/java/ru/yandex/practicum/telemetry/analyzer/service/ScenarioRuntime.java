package ru.yandex.practicum.telemetry.analyzer.service;

import java.util.List;

public record ScenarioRuntime(
        String hubId,
        String name,
        List<ConditionCheck> conditions,
        List<ActionCmd> actions
) {
    public record ConditionCheck(String sensorId, String type, String operation, int value) {
    }

    public record ActionCmd(String sensorId, String type, int value) {
    }
}