package ru.yandex.practicum.telemetry.aggregator.service;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.WakeupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.telemetry.aggregator.config.AggregatorKafkaProperties;
import ru.yandex.practicum.telemetry.aggregator.kafka.AvroBinaryConverter;

@Component
public class AggregationStarter {

    private static final Logger log = LoggerFactory.getLogger(AggregationStarter.class);

    private final KafkaConsumer<String, SensorEventAvro> consumer;
    private final KafkaProducer<String, byte[]> producer;
    private final AggregatorKafkaProperties properties;
    private final SnapshotAggregationService snapshotAggregationService;
    private final AvroBinaryConverter avroBinaryConverter;
    private final AtomicBoolean running = new AtomicBoolean(true);

    public AggregationStarter(KafkaConsumer<String, SensorEventAvro> consumer,
                              KafkaProducer<String, byte[]> producer,
                              AggregatorKafkaProperties properties,
                              SnapshotAggregationService snapshotAggregationService,
                              AvroBinaryConverter avroBinaryConverter) {
        this.consumer = consumer;
        this.producer = producer;
        this.properties = properties;
        this.snapshotAggregationService = snapshotAggregationService;
        this.avroBinaryConverter = avroBinaryConverter;
    }

    public void start() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop, "aggregator-shutdown"));
        consumer.subscribe(List.of(properties.getTopics().getSensors()));

        try {
            while (running.get()) {
                ConsumerRecords<String, SensorEventAvro> records = consumer.poll(properties.getPollTimeout());

                if (records.isEmpty()) {
                    continue;
                }

                for (ConsumerRecord<String, SensorEventAvro> record : records) {
                    processEvent(record.value());
                }

                producer.flush();
                consumer.commitSync();
            }
        } catch (WakeupException exception) {
            if (running.get()) {
                throw exception;
            }
        } catch (Exception exception) {
            log.error("Error during sensor events aggregation", exception);
        } finally {
            shutdownClients();
        }
    }

    public void stop() {
        if (running.compareAndSet(true, false)) {
            consumer.wakeup();
        }
    }

    private void processEvent(SensorEventAvro event) {
        snapshotAggregationService.updateState(event)
                .ifPresent(this::publishSnapshot);
    }

    private void publishSnapshot(SensorsSnapshotAvro snapshot) {
        ProducerRecord<String, byte[]> record = new ProducerRecord<>(
                properties.getTopics().getSnapshots(),
                snapshot.getHubId(),
                avroBinaryConverter.toBytes(snapshot)
        );

        try {
            producer.send(record).get();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while publishing snapshot", exception);
        } catch (ExecutionException exception) {
            throw new IllegalStateException("Could not publish snapshot to Kafka", exception);
        }
    }

    private void shutdownClients() {
        try {
            producer.flush();
            consumer.commitSync();
        } catch (Exception exception) {
            log.warn("Could not finalize Kafka state before shutdown", exception);
        } finally {
            consumer.close();
            producer.close();
        }
    }
}
