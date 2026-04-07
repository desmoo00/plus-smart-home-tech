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
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.telemetry.analyzer.config.AnalyzerKafkaProperties;
import ru.yandex.practicum.telemetry.analyzer.kafka.AnalyzerConsumerFactory;
import ru.yandex.practicum.telemetry.analyzer.kafka.HubEventDeserializer;
import ru.yandex.practicum.telemetry.analyzer.service.HubEventService;

@Component
public class HubEventProcessor implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(HubEventProcessor.class);

    private final AnalyzerKafkaProperties properties;
    private final AnalyzerConsumerFactory consumerFactory;
    private final HubEventDeserializer deserializer;
    private final HubEventService hubEventService;
    private final AtomicBoolean running = new AtomicBoolean(true);
    private volatile KafkaConsumer<String, byte[]> consumer;

    public HubEventProcessor(AnalyzerKafkaProperties properties,
                             AnalyzerConsumerFactory consumerFactory,
                             HubEventDeserializer deserializer,
                             HubEventService hubEventService) {
        this.properties = properties;
        this.consumerFactory = consumerFactory;
        this.deserializer = deserializer;
        this.hubEventService = hubEventService;
    }

    // Слушаем и обновляем события хаба
    @Override
    public void run() {
        consumer = consumerFactory.createHubConsumer();
        consumer.subscribe(Collections.singletonList(properties.getTopics().getHubs()));
        Duration pollTimeout = properties.getPollTimeout();

        try {
            while (running.get()) {
                ConsumerRecords<String, byte[]> records = consumer.poll(pollTimeout);
                for (ConsumerRecord<String, byte[]> record : records) {
                    HubEventAvro hubEvent = deserializer.deserialize(record.value());
                    hubEventService.handle(hubEvent);
                }
                if (!records.isEmpty()) {
                    consumer.commitAsync();
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
            log.debug("Unable to commit hub events consumer before shutdown", exception);
        } finally {
            currentConsumer.close();
        }
    }
}
