package ru.yandex.practicum.telemetry.analyzer.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.kafka.telemetry.event.HubSnapshotAvro;
import ru.yandex.practicum.kafka.telemetry.event.LightSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.MotionSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.TemperatureSensorAvro;
import ru.yandex.practicum.telemetry.analyzer.model.Action;
import ru.yandex.practicum.telemetry.analyzer.model.ActionType;
import ru.yandex.practicum.telemetry.analyzer.model.Condition;
import ru.yandex.practicum.telemetry.analyzer.model.ConditionOperation;
import ru.yandex.practicum.telemetry.analyzer.model.ConditionType;
import ru.yandex.practicum.telemetry.analyzer.model.Scenario;
import ru.yandex.practicum.telemetry.analyzer.model.ScenarioActionLink;
import ru.yandex.practicum.telemetry.analyzer.model.ScenarioConditionLink;
import ru.yandex.practicum.telemetry.analyzer.model.Sensor;

class ScenarioEvaluatorTest {

    private final ScenarioEvaluator evaluator = new ScenarioEvaluator();

    @Test
    void shouldReturnActionWhenScenarioMatchesSnapshot() {
        Scenario scenario = new Scenario("hub-1", "Auto light");
        Sensor motionSensor = new Sensor("motion-1", "hub-1");
        Sensor lightSensor = new Sensor("light-1", "hub-1");
        Sensor switchSensor = new Sensor("switch-1", "hub-1");

        scenario.addCondition(new ScenarioConditionLink(
                motionSensor,
                new Condition(ConditionType.MOTION, ConditionOperation.EQUALS, 1)
        ));
        scenario.addCondition(new ScenarioConditionLink(
                lightSensor,
                new Condition(ConditionType.LUMINOSITY, ConditionOperation.LOWER_THAN, 500)
        ));
        scenario.addAction(new ScenarioActionLink(
                switchSensor,
                new Action(ActionType.ACTIVATE, null)
        ));

        HubSnapshotAvro snapshot = HubSnapshotAvro.newBuilder()
                .setHubId("hub-1")
                .setTimestamp(Instant.parse("2026-04-07T10:15:30Z"))
                .setSensorsState(Map.of(
                        "motion-1", MotionSensorAvro.newBuilder()
                                .setLinkQuality(100)
                                .setMotion(true)
                                .setVoltage(220)
                                .build(),
                        "light-1", LightSensorAvro.newBuilder()
                                .setLinkQuality(90)
                                .setLuminosity(120)
                                .build()
                ))
                .build();

        List<ScenarioActionCommand> commands = evaluator.evaluate(snapshot, List.of(scenario));

        assertEquals(1, commands.size());
        assertEquals("switch-1", commands.getFirst().sensorId());
        assertEquals(ActionType.ACTIVATE, commands.getFirst().type());
    }

    @Test
    void shouldIgnoreScenarioWhenConditionDoesNotMatch() {
        Scenario scenario = new Scenario("hub-1", "Warm floor");
        Sensor temperatureSensor = new Sensor("temperature-1", "hub-1");
        Sensor switchSensor = new Sensor("switch-1", "hub-1");

        scenario.addCondition(new ScenarioConditionLink(
                temperatureSensor,
                new Condition(ConditionType.TEMPERATURE, ConditionOperation.LOWER_THAN, 15)
        ));
        scenario.addAction(new ScenarioActionLink(
                switchSensor,
                new Action(ActionType.ACTIVATE, null)
        ));

        HubSnapshotAvro snapshot = HubSnapshotAvro.newBuilder()
                .setHubId("hub-1")
                .setTimestamp(Instant.parse("2026-04-07T10:15:30Z"))
                .setSensorsState(Map.of(
                        "temperature-1", TemperatureSensorAvro.newBuilder()
                                .setId("temperature-1")
                                .setHubId("hub-1")
                                .setTimestamp(Instant.parse("2026-04-07T10:00:00Z"))
                                .setTemperatureC(22)
                                .setTemperatureF(71)
                                .build()
                ))
                .build();

        List<ScenarioActionCommand> commands = evaluator.evaluate(snapshot, List.of(scenario));

        assertEquals(0, commands.size());
    }
}
