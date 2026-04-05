package ru.yandex.practicum.telemetry.collector.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.telemetry.collector.kafka.AvroBinaryConverter;
import ru.yandex.practicum.telemetry.collector.kafka.CollectorKafkaProducer;
import ru.yandex.practicum.telemetry.collector.kafka.CollectorTopicsProperties;
import ru.yandex.practicum.telemetry.collector.mapper.HubEventAvroMapper;
import ru.yandex.practicum.telemetry.collector.mapper.SensorEventAvroMapper;
import ru.yandex.practicum.telemetry.collector.model.hub.HubEvent;
import ru.yandex.practicum.telemetry.collector.model.sensor.SensorEvent;

@Service
public class CollectorService {

    private final SensorEventAvroMapper sensorEventAvroMapper;
    private final HubEventAvroMapper hubEventAvroMapper;
    private final AvroBinaryConverter avroBinaryConverter;
    private final CollectorKafkaProducer collectorKafkaProducer;
    private final CollectorTopicsProperties topicsProperties;

    public CollectorService(SensorEventAvroMapper sensorEventAvroMapper,
                            HubEventAvroMapper hubEventAvroMapper,
                            AvroBinaryConverter avroBinaryConverter,
                            CollectorKafkaProducer collectorKafkaProducer,
                            CollectorTopicsProperties topicsProperties) {
        this.sensorEventAvroMapper = sensorEventAvroMapper;
        this.hubEventAvroMapper = hubEventAvroMapper;
        this.avroBinaryConverter = avroBinaryConverter;
        this.collectorKafkaProducer = collectorKafkaProducer;
        this.topicsProperties = topicsProperties;
    }

    public void saveSensorEvent(SensorEvent event) {
        SensorEventAvro avroEvent = sensorEventAvroMapper.toAvro(event);

        // Все sensor-события складываем в один топик, чтобы дальше их забирал Aggregator.
        collectorKafkaProducer.send(
                topicsProperties.getSensors(),
                event.getHubId(),
                avroBinaryConverter.toBytes(avroEvent)
        );
    }

    public void saveHubEvent(HubEvent event) {
        HubEventAvro avroEvent = hubEventAvroMapper.toAvro(event);

        // События хаба и сценариев живут отдельно от телеметрии датчиков.
        collectorKafkaProducer.send(
                topicsProperties.getHubs(),
                event.getHubId(),
                avroBinaryConverter.toBytes(avroEvent)
        );
    }
}
