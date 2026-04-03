package ru.yandex.practicum.telemetry.analyzer.service;

import ru.yandex.practicum.kafka.telemetry.event.ActionTypeAvro;

import java.util.List;

public record ScenarioRuntime(
        Long id,
        String hubId,
        String name,
        List<ConditionCheck> conditions,
        List<ActionCmd> actions
) {
    public record ConditionCheck(
            String sensorId,
            String type,
            String operation,
            Integer value
    ) {}

    public record ActionCmd(
            String deviceId,
            ActionTypeAvro type,
            Integer value
    ) {}
}