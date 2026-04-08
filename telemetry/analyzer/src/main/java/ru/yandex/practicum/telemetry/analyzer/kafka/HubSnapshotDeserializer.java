package ru.yandex.practicum.telemetry.analyzer.kafka;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

@Component
public class HubSnapshotDeserializer extends BaseAvroDeserializer<SensorsSnapshotAvro> {

    public HubSnapshotDeserializer() {
        super(SensorsSnapshotAvro.class);
    }
}
