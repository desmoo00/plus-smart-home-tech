package ru.yandex.practicum.telemetry.aggregator.service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

@Service
public class SnapshotAggregationService {

    private final Map<String, SensorsSnapshotAvro> snapshotsByHubId = new HashMap<>();

    public Optional<SensorsSnapshotAvro> updateState(SensorEventAvro event) {
        SensorsSnapshotAvro snapshot = snapshotsByHubId.computeIfAbsent(event.getHubId(), this::newSnapshot);

        Map<String, SensorStateAvro> sensorsState = snapshot.getSensorsState();
        if (sensorsState == null) {
            sensorsState = new HashMap<>();
            snapshot.setSensorsState(sensorsState);
        }

        SensorStateAvro oldState = sensorsState.get(event.getId());
        if (oldState != null) {
            boolean isOlderEvent = oldState.getTimestamp().isAfter(event.getTimestamp());
            boolean payloadUnchanged = oldState.getData().equals(event.getPayload());
            if (isOlderEvent || payloadUnchanged) {
                return Optional.empty();
            }
        }

        SensorStateAvro newState = SensorStateAvro.newBuilder()
                .setTimestamp(event.getTimestamp())
                .setData(event.getPayload())
                .build();

        sensorsState.put(event.getId(), newState);
        snapshot.setTimestamp(event.getTimestamp());
        return Optional.of(snapshot);
    }

    private SensorsSnapshotAvro newSnapshot(String hubId) {
        return SensorsSnapshotAvro.newBuilder()
                .setHubId(hubId)
                .setTimestamp(Instant.EPOCH)
                .setSensorsState(new HashMap<>())
                .build();
    }
}
