package ru.yandex.practicum.telemetry.analyzer.service;

import java.time.Instant;
import ru.yandex.practicum.telemetry.analyzer.model.ActionType;

public record ScenarioActionCommand(
        String hubId,
        String scenarioName,
        String sensorId,
        ActionType type,
        Integer value,
        Instant timestamp
) {
}
