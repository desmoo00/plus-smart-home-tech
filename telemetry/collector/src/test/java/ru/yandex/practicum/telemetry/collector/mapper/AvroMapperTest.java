package ru.yandex.practicum.telemetry.collector.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.telemetry.collector.model.hub.ActionType;
import ru.yandex.practicum.telemetry.collector.model.hub.ConditionOperation;
import ru.yandex.practicum.telemetry.collector.model.hub.ConditionType;
import ru.yandex.practicum.telemetry.collector.model.hub.DeviceAction;
import ru.yandex.practicum.telemetry.collector.model.hub.DeviceAddedEvent;
import ru.yandex.practicum.telemetry.collector.model.hub.DeviceType;
import ru.yandex.practicum.telemetry.collector.model.hub.HubEventType;
import ru.yandex.practicum.telemetry.collector.model.hub.ScenarioAddedEvent;
import ru.yandex.practicum.telemetry.collector.model.hub.ScenarioCondition;
import ru.yandex.practicum.telemetry.collector.model.sensor.ClimateSensorEvent;
import ru.yandex.practicum.telemetry.collector.model.sensor.SensorEventType;

class AvroMapperTest {

    private final SensorEventAvroMapper sensorEventAvroMapper = new SensorEventAvroMapper();
    private final HubEventAvroMapper hubEventAvroMapper = new HubEventAvroMapper();

    @Test
    void shouldMapClimateSensorEventToAvro() {
        ClimateSensorEvent event = new ClimateSensorEvent();
        event.setId("sensor-1");
        event.setHubId("hub-1");
        event.setTimestamp(Instant.parse("2024-08-06T15:11:24.157Z"));
        event.setTemperatureC(21);
        event.setHumidity(45);
        event.setCo2Level(600);
        event.setType(SensorEventType.CLIMATE_SENSOR_EVENT);

        var avro = sensorEventAvroMapper.toAvro(event);

        assertEquals("sensor-1", avro.getId());
        assertInstanceOf(ClimateSensorAvro.class, avro.getPayload());
        ClimateSensorAvro payload = (ClimateSensorAvro) avro.getPayload();
        assertEquals(21, payload.getTemperatureC());
    }

    @Test
    void shouldMapDeviceAddedEventToAvro() {
        DeviceAddedEvent event = new DeviceAddedEvent();
        event.setHubId("hub-1");
        event.setId("device-1");
        event.setDeviceType(DeviceType.LIGHT_SENSOR);
        event.setType(HubEventType.DEVICE_ADDED);

        var avro = hubEventAvroMapper.toAvro(event);

        assertEquals("hub-1", avro.getHubId());
        assertInstanceOf(DeviceAddedEventAvro.class, avro.getPayload());
        DeviceAddedEventAvro payload = (DeviceAddedEventAvro) avro.getPayload();
        assertEquals(DeviceTypeAvro.LIGHT_SENSOR, payload.getType());
    }

    @Test
    void shouldMapScenarioAddedEventToAvro() {
        ScenarioCondition condition = new ScenarioCondition();
        condition.setSensorId("sensor-1");
        condition.setType(ConditionType.TEMPERATURE);
        condition.setOperation(ConditionOperation.GREATER_THAN);
        condition.setValue(20);

        DeviceAction action = new DeviceAction();
        action.setSensorId("switch-1");
        action.setType(ActionType.SET_VALUE);
        action.setValue(1);

        ScenarioAddedEvent event = new ScenarioAddedEvent();
        event.setHubId("hub-1");
        event.setName("warm-room");
        event.setConditions(List.of(condition));
        event.setActions(List.of(action));
        event.setType(HubEventType.SCENARIO_ADDED);

        var avro = hubEventAvroMapper.toAvro(event);

        assertInstanceOf(ScenarioAddedEventAvro.class, avro.getPayload());
        ScenarioAddedEventAvro payload = (ScenarioAddedEventAvro) avro.getPayload();
        assertEquals(1, payload.getConditions().size());
        assertEquals(1, payload.getActions().size());
    }

    @Test
    void shouldMapScenarioConditionBooleanValueToAvro() {
        ScenarioCondition condition = new ScenarioCondition();
        condition.setSensorId("sensor-switch-1");
        condition.setType(ConditionType.SWITCH);
        condition.setOperation(ConditionOperation.EQUALS);
        condition.setValue(true);

        DeviceAction action = new DeviceAction();
        action.setSensorId("switch-1");
        action.setType(ActionType.ACTIVATE);

        ScenarioAddedEvent event = new ScenarioAddedEvent();
        event.setHubId("hub-1");
        event.setName("armed");
        event.setConditions(List.of(condition));
        event.setActions(List.of(action));
        event.setType(HubEventType.SCENARIO_ADDED);

        var avro = hubEventAvroMapper.toAvro(event);

        assertInstanceOf(ScenarioAddedEventAvro.class, avro.getPayload());
        ScenarioAddedEventAvro payload = (ScenarioAddedEventAvro) avro.getPayload();
        assertEquals(Boolean.TRUE, payload.getConditions().getFirst().getValue());
    }
}
