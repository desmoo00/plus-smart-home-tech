package ru.yandex.practicum.telemetry.collector.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.telemetry.collector.model.hub.DeviceAddedEvent;
import ru.yandex.practicum.telemetry.collector.model.hub.HubEvent;
import ru.yandex.practicum.telemetry.collector.model.hub.HubEventType;
import ru.yandex.practicum.telemetry.collector.model.hub.ScenarioAddedEvent;
import ru.yandex.practicum.telemetry.collector.model.sensor.MotionSensorEvent;
import ru.yandex.practicum.telemetry.collector.model.sensor.SensorEvent;
import ru.yandex.practicum.telemetry.collector.model.sensor.SensorEventType;

class EventJsonDeserializationTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void shouldDeserializeSensorEventByType() throws Exception {
        String json = """
                {
                  "id": "sensor-1",
                  "hubId": "hub-1",
                  "timestamp": "2024-08-06T15:11:24.157Z",
                  "linkQuality": 75,
                  "motion": true,
                  "voltage": 220,
                  "type": "MOTION_SENSOR_EVENT"
                }
                """;

        SensorEvent event = objectMapper.readValue(json, SensorEvent.class);

        assertInstanceOf(MotionSensorEvent.class, event);
        assertEquals(SensorEventType.MOTION_SENSOR_EVENT, event.getType());
        assertEquals("sensor-1", event.getId());
    }

    @Test
    void shouldDeserializeHubEventByType() throws Exception {
        String json = """
                {
                  "hubId": "hub-1",
                  "timestamp": "2024-08-06T15:11:24.157Z",
                  "id": "device-1",
                  "deviceType": "LIGHT_SENSOR",
                  "type": "DEVICE_ADDED"
                }
                """;

        HubEvent event = objectMapper.readValue(json, HubEvent.class);

        assertInstanceOf(DeviceAddedEvent.class, event);
        assertEquals(HubEventType.DEVICE_ADDED, event.getType());
        assertEquals("hub-1", event.getHubId());
    }

    @Test
    void shouldDeserializeScenarioConditionBooleanValue() throws Exception {
        String json = """
                {
                  "hubId": "hub-1",
                  "timestamp": "2024-08-06T15:11:24.157Z",
                  "name": "night-mode",
                  "conditions": [
                    {
                      "sensorId": "sensor-switch-1",
                      "type": "SWITCH",
                      "operation": "EQUALS",
                      "value": true
                    }
                  ],
                  "actions": [
                    {
                      "sensorId": "switch-1",
                      "type": "ACTIVATE"
                    }
                  ],
                  "type": "SCENARIO_ADDED"
                }
                """;

        HubEvent event = objectMapper.readValue(json, HubEvent.class);

        assertInstanceOf(ScenarioAddedEvent.class, event);
        ScenarioAddedEvent scenarioAddedEvent = (ScenarioAddedEvent) event;
        assertEquals(Boolean.TRUE, scenarioAddedEvent.getConditions().getFirst().getValue());
    }
}
