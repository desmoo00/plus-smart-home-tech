package ru.yandex.practicum.telemetry.collector.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

@SpringBootTest
@AutoConfigureMockMvc
@EmbeddedKafka(partitions = 1, topics = {"telemetry.sensors.v1", "telemetry.hubs.v1"})
@TestPropertySource(properties = "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}")
class CollectorControllerKafkaIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    private Consumer<String, byte[]> consumer;

    @AfterEach
    void tearDown() {
        if (consumer != null) {
            consumer.close();
        }
    }

    @Test
    void shouldSaveSensorEventToKafka() throws Exception {
        consumer = createConsumer("telemetry.sensors.v1");

        String json = """
                {
                  "id": "sensor-1",
                  "hubId": "hub-1",
                  "timestamp": "2024-08-06T15:11:24.157Z",
                  "linkQuality": 75,
                  "motion": true,
                  "voltage": 220,
                  "type": "MOTION_SENSOR_EVENT"
                }
                """;

        mockMvc.perform(post("/events/sensors")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isOk());

        ConsumerRecord<String, byte[]> record = pollSingleRecord("telemetry.sensors.v1");
        SensorEventAvro eventAvro = readSensorEvent(record.value());

        assertThat(record.key()).isEqualTo("hub-1");
        assertThat(eventAvro.getId()).isEqualTo("sensor-1");
        assertThat(eventAvro.getHubId()).isEqualTo("hub-1");
    }

    @Test
    void shouldSaveHubEventToKafka() throws Exception {
        consumer = createConsumer("telemetry.hubs.v1");

        String json = """
                {
                  "hubId": "hub-1",
                  "timestamp": "2024-08-06T15:11:24.157Z",
                  "id": "device-1",
                  "deviceType": "LIGHT_SENSOR",
                  "type": "DEVICE_ADDED"
                }
                """;

        mockMvc.perform(post("/events/hubs")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isOk());

        ConsumerRecord<String, byte[]> record = pollSingleRecord("telemetry.hubs.v1");
        HubEventAvro eventAvro = readHubEvent(record.value());

        assertThat(record.key()).isEqualTo("hub-1");
        assertThat(eventAvro.getHubId()).isEqualTo("hub-1");
    }

    @Test
    void shouldReturnBadRequestForUnknownType() throws Exception {
        String json = """
                {
                  "id": "sensor-1",
                  "hubId": "hub-1",
                  "type": "UNKNOWN_TYPE"
                }
                """;

        mockMvc.perform(post("/events/sensors")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    private Consumer<String, byte[]> createConsumer(String topic) {
        Map<String, Object> props = new HashMap<>(KafkaTestUtils.consumerProps("test-group", "true", embeddedKafkaBroker));
        props.put(org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);

        Consumer<String, byte[]> kafkaConsumer = new DefaultKafkaConsumerFactory<String, byte[]>(props).createConsumer();
        embeddedKafkaBroker.consumeFromAnEmbeddedTopic(kafkaConsumer, topic);
        return kafkaConsumer;
    }

    private ConsumerRecord<String, byte[]> pollSingleRecord(String topic) {
        for (int attempt = 0; attempt < 10; attempt++) {
            ConsumerRecords<String, byte[]> records = consumer.poll(Duration.ofSeconds(1));
            for (ConsumerRecord<String, byte[]> record : records) {
                if (topic.equals(record.topic())) {
                    return record;
                }
            }
        }

        throw new AssertionError("No record found in topic " + topic);
    }

    private SensorEventAvro readSensorEvent(byte[] bytes) throws Exception {
        SpecificDatumReader<SensorEventAvro> reader = new SpecificDatumReader<>(SensorEventAvro.class);
        BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(bytes, null);
        return reader.read(null, decoder);
    }

    private HubEventAvro readHubEvent(byte[] bytes) throws Exception {
        SpecificDatumReader<HubEventAvro> reader = new SpecificDatumReader<>(HubEventAvro.class);
        BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(bytes, null);
        return reader.read(null, decoder);
    }
}
