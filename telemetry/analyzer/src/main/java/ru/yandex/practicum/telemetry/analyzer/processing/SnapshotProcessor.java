package ru.yandex.practicum.telemetry.analyzer.processing;

import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.telemetry.analyzer.config.AnalyzerKafkaProperties;
import ru.yandex.practicum.telemetry.analyzer.kafka.AnalyzerConsumerFactory;
import ru.yandex.practicum.telemetry.analyzer.kafka.HubSnapshotDeserializer;
import ru.yandex.practicum.telemetry.analyzer.service.SnapshotService;

@Component
public class SnapshotProcessor {

    private static final Logger log = LoggerFactory.getLogger(SnapshotProcessor.class);

    private final AnalyzerKafkaProperties properties;
    private final AnalyzerConsumerFactory consumerFactory;
    private final HubSnapshotDeserializer deserializer;
    private final SnapshotService snapshotService;
    private final AtomicBoolean running = new AtomicBoolean(true);
    private volatile KafkaConsumer<String, byte[]> consumer;

    public SnapshotProcessor(AnalyzerKafkaProperties properties,
                             AnalyzerConsumerFactory consumerFactory,
                             HubSnapshotDeserializer deserializer,
                             SnapshotService snapshotService) {
        this.properties = properties;
        this.consumerFactory = consumerFactory;
        this.deserializer = deserializer;
        this.snapshotService = snapshotService;
    }

    public void start() {
        consumer = consumerFactory.createSnapshotConsumer();
        consumer.subscribe(Collections.singletonList(properties.getTopics().getSnapshots()));
        Duration pollTimeout = properties.getPollTimeout();

        try {
            while (running.get()) {
                ConsumerRecords<String, byte[]> records = consumer.poll(pollTimeout);
                for (ConsumerRecord<String, byte[]> record : records) {
                    SensorsSnapshotAvro snapshot = deserializer.deserialize(record.value());
                    snapshotService.handle(snapshot);
                }
                if (!records.isEmpty()) {
                    consumer.commitSync();
                }
            }
        } catch (WakeupException exception) {
            if (running.get()) {
                throw exception;
            }
        } finally {
            closeConsumer();
        }
    }

    public void shutdown() {
        running.set(false);
        KafkaConsumer<String, byte[]> currentConsumer = consumer;
        if (currentConsumer != null) {
            currentConsumer.wakeup();
        }
    }

    private void closeConsumer() {
        KafkaConsumer<String, byte[]> currentConsumer = consumer;
        if (currentConsumer == null) {
            return;
        }
        try {
            currentConsumer.commitSync();
        } catch (Exception exception) {
            log.debug("Unable to commit snapshots consumer before shutdown", exception);
        } finally {
            currentConsumer.close();
        }
    }
}
