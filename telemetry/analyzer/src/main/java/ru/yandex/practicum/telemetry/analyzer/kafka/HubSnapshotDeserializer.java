package ru.yandex.practicum.telemetry.analyzer.kafka;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.HubSnapshotAvro;

@Component
public class HubSnapshotDeserializer extends BaseAvroDeserializer<HubSnapshotAvro> {

    public HubSnapshotDeserializer() {
        super(HubSnapshotAvro.class);
    }
}
