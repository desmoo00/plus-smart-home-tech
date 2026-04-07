package ru.yandex.practicum.telemetry.analyzer.service;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubSnapshotAvro;
import ru.yandex.practicum.kafka.telemetry.event.LightSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.MotionSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SwitchSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.TemperatureSensorAvro;
import ru.yandex.practicum.telemetry.analyzer.model.ConditionOperation;
import ru.yandex.practicum.telemetry.analyzer.model.ConditionType;
import ru.yandex.practicum.telemetry.analyzer.model.Scenario;
import ru.yandex.practicum.telemetry.analyzer.model.ScenarioConditionLink;

@Component
public class ScenarioEvaluator {

    public List<ScenarioActionCommand> evaluate(HubSnapshotAvro snapshot, List<Scenario> scenarios) {
        if (snapshot == null || scenarios == null || scenarios.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, Object> sensorsState = snapshot.getSensorsState();
        List<ScenarioActionCommand> commands = new LinkedList<>();

        for (Scenario scenario : scenarios) {
            boolean allConditionsMatched = true;
            for (ScenarioConditionLink condition : scenario.getConditions()) {
                if (!matches(condition, sensorsState)) {
                    allConditionsMatched = false;
                    break;
                }
            }

            if (!allConditionsMatched) {
                continue;
            }

            scenario.getActions().forEach(action -> commands.add(new ScenarioActionCommand(
                    snapshot.getHubId(),
                    scenario.getName(),
                    action.getSensor().getId(),
                    action.getAction().getType(),
                    action.getAction().getValue(),
                    snapshot.getTimestamp()
            )));
        }

        return commands;
    }

    private boolean matches(ScenarioConditionLink link, Map<String, Object> sensorsState) {
        Object state = sensorsState.get(link.getSensor().getId());
        if (state == null) {
            return false;
        }

        OptionalInt actualValue = extractValue(link.getCondition().getType(), state);
        return actualValue.isPresent() && compare(
                actualValue.getAsInt(),
                link.getCondition().getOperation(),
                link.getCondition().getValue()
        );
    }

    private OptionalInt extractValue(ConditionType type, Object state) {
        return switch (type) {
            case MOTION -> state instanceof MotionSensorAvro motion
                    ? OptionalInt.of(booleanToInt(motion.getMotion()))
                    : OptionalInt.empty();
            case LUMINOSITY -> state instanceof LightSensorAvro light
                    ? OptionalInt.of(light.getLuminosity())
                    : OptionalInt.empty();
            case SWITCH -> state instanceof SwitchSensorAvro sensor
                    ? OptionalInt.of(booleanToInt(sensor.getState()))
                    : OptionalInt.empty();
            case TEMPERATURE -> extractTemperature(state);
            case CO2LEVEL -> state instanceof ClimateSensorAvro climate
                    ? OptionalInt.of(climate.getCo2Level())
                    : OptionalInt.empty();
            case HUMIDITY -> state instanceof ClimateSensorAvro climate
                    ? OptionalInt.of(climate.getHumidity())
                    : OptionalInt.empty();
        };
    }

    private OptionalInt extractTemperature(Object state) {
        if (state instanceof TemperatureSensorAvro temperature) {
            return OptionalInt.of(temperature.getTemperatureC());
        }
        if (state instanceof ClimateSensorAvro climate) {
            return OptionalInt.of(climate.getTemperatureC());
        }
        return OptionalInt.empty();
    }

    // Сравнение с порогом из сценария по нужной операции.
    private boolean compare(int actual, ConditionOperation operation, Integer threshold) {
        if (threshold == null) {
            return false;
        }
        return switch (operation) {
            case EQUALS -> actual == threshold;
            case GREATER_THAN -> actual > threshold;
            case LOWER_THAN -> actual < threshold;
        };
    }

    private int booleanToInt(boolean value) {
        return value ? 1 : 0;
    }
}
