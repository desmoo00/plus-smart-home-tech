package ru.yandex.practicum.telemetry.collector.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.telemetry.collector.exception.KafkaPublishException;

@Component
public class CollectorKafkaProducer {

    private static final Logger log = LoggerFactory.getLogger(CollectorKafkaProducer.class);

    private final KafkaTemplate<String, byte[]> kafkaTemplate;

    public CollectorKafkaProducer(KafkaTemplate<String, byte[]> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(String topic, String key, byte[] payload) {
        try {
            // Отправим асинхронно, чтобы не блокировать поток запроса
            kafkaTemplate.send(topic, key, payload)
                    .whenComplete((result, exception) -> {
                        if (exception != null) {
                            log.error("could not publish event to kafka topic {}", topic, exception);
                        }
                    });
        } catch (Exception exception) {
            throw new KafkaPublishException("Could not publish event to Kafka topic " + topic, exception);
        }
    }
}
