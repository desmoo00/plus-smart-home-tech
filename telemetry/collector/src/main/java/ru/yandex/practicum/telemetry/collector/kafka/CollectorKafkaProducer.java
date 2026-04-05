package ru.yandex.practicum.telemetry.collector.kafka;

import java.util.concurrent.TimeUnit;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.telemetry.collector.exception.KafkaPublishException;

@Component
public class CollectorKafkaProducer {

    private final KafkaTemplate<String, byte[]> kafkaTemplate;

    public CollectorKafkaProducer(KafkaTemplate<String, byte[]> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(String topic, String key, byte[] payload) {
        try {
            kafkaTemplate.send(topic, key, payload).get(10, TimeUnit.SECONDS);
        } catch (Exception exception) {
            throw new KafkaPublishException("Could not publish event to Kafka topic " + topic, exception);
        }
    }
}
