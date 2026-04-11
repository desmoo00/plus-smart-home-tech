package ru.yandex.practicum.telemetry.aggregator.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.kafka.telemetry.event.LightSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.MotionSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

class SnapshotAggregationServiceTest {

    private SnapshotAggregationService aggregationService;

    @BeforeEach
    void setUp() {
        aggregationService = new SnapshotAggregationService();
    }

    @Test
    void shouldCreateSnapshotForFirstSensorEvent() {
        SensorEventAvro event = lightEvent("sensor-1", "hub-1", Instant.parse("2024-01-01T10:00:00Z"), 40);

        SensorsSnapshotAvro snapshot = aggregationService.updateState(event).orElseThrow();

        assertEquals("hub-1", snapshot.getHubId());
        assertEquals(event.getTimestamp(), snapshot.getTimestamp());
        assertEquals(1, snapshot.getSensorsState().size());
        assertEquals(event.getPayload(), snapshot.getSensorsState().get("sensor-1").getData());
    }

    @Test
    void shouldIgnoreOlderEventForKnownSensor() {
        SensorEventAvro actualEvent = motionEvent("sensor-1", "hub-1", Instant.parse("2024-01-01T10:01:00Z"), true);
        SensorEventAvro olderEvent = motionEvent("sensor-1", "hub-1", Instant.parse("2024-01-01T10:00:00Z"), false);

        aggregationService.updateState(actualEvent);

        assertTrue(aggregationService.updateState(olderEvent).isEmpty());
    }

    @Test
    void shouldIgnoreSamePayloadForKnownSensor() {
        SensorEventAvro firstEvent = lightEvent("sensor-1", "hub-1", Instant.parse("2024-01-01T10:00:00Z"), 15);
        SensorEventAvro duplicateEvent = lightEvent("sensor-1", "hub-1", Instant.parse("2024-01-01T10:01:00Z"), 15);

        aggregationService.updateState(firstEvent);

        assertFalse(aggregationService.updateState(duplicateEvent).isPresent());
    }

    @Test
    void shouldUpdateSnapshotWhenPayloadChanges() {
        SensorEventAvro firstEvent = motionEvent("sensor-1", "hub-1", Instant.parse("2024-01-01T10:00:00Z"), false);
        SensorEventAvro updatedEvent = motionEvent("sensor-1", "hub-1", Instant.parse("2024-01-01T10:01:00Z"), true);

        aggregationService.updateState(firstEvent);
        SensorsSnapshotAvro snapshot = aggregationService.updateState(updatedEvent).orElseThrow();

        assertEquals(updatedEvent.getTimestamp(), snapshot.getTimestamp());
        assertEquals(updatedEvent.getPayload(), snapshot.getSensorsState().get("sensor-1").getData());
    }

    private SensorEventAvro lightEvent(String sensorId, String hubId, Instant timestamp, int luminosity) {
        return SensorEventAvro.newBuilder()
                .setId(sensorId)
                .setHubId(hubId)
                .setTimestamp(timestamp)
                .setPayload(LightSensorAvro.newBuilder()
                        .setLinkQuality(100)
                        .setLuminosity(luminosity)
                        .build())
                .build();
    }

    private SensorEventAvro motionEvent(String sensorId, String hubId, Instant timestamp, boolean motion) {
        return SensorEventAvro.newBuilder()
                .setId(sensorId)
                .setHubId(hubId)
                .setTimestamp(timestamp)
                .setPayload(MotionSensorAvro.newBuilder()
                        .setLinkQuality(80)
                        .setMotion(motion)
                        .setVoltage(220)
                        .build())
                .build();
    }
}
