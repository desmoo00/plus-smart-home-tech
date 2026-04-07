package ru.yandex.practicum.telemetry.analyzer.kafka;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.telemetry.analyzer.config.AnalyzerKafkaProperties;

@Component
public class AnalyzerConsumerFactory {

    private final AnalyzerKafkaProperties properties;

    public AnalyzerConsumerFactory(AnalyzerKafkaProperties properties) {
        this.properties = properties;
    }

    public KafkaConsumer<String, byte[]> createSnapshotConsumer() {
        return new KafkaConsumer<>(buildProperties(properties.getConsumers().getSnapshots()));
    }

    public KafkaConsumer<String, byte[]> createHubConsumer() {
        return new KafkaConsumer<>(buildProperties(properties.getConsumers().getHubs()));
    }

    private Properties buildProperties(AnalyzerKafkaProperties.ConsumerSettings consumerSettings) {
        Map<String, Object> merged = new HashMap<>(properties.getProperties());
        merged.putAll(consumerSettings.getProperties());
        merged.put(ConsumerConfig.GROUP_ID_CONFIG, consumerSettings.getGroupId());
        merged.putIfAbsent(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        merged.putIfAbsent(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());
        merged.putIfAbsent(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        Properties kafkaProperties = new Properties();
        kafkaProperties.putAll(merged);
        return kafkaProperties;
    }
}
