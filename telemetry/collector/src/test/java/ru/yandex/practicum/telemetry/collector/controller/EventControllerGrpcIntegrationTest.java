package ru.yandex.practicum.telemetry.collector.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import java.time.Duration;
import java.time.Instant;
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
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import net.devh.boot.grpc.server.event.GrpcServerStartedEvent;
import ru.yandex.practicum.grpc.telemetry.collector.CollectorControllerGrpc;
import ru.yandex.practicum.grpc.telemetry.event.DeviceAddedProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceTypeProto;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.MotionSensorProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

@SpringBootTest(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.cloud.config.enabled=false",
        "eureka.client.enabled=false",
        "grpc.server.port=0",
        "collector.topics.sensors=telemetry.sensors.v1",
        "collector.topics.hubs=telemetry.hubs.v1",
        "collector.topics.partitions=1",
        "collector.topics.replicas=1"
})
@EmbeddedKafka(partitions = 1, topics = {"telemetry.sensors.v1", "telemetry.hubs.v1"})
@Import(EventControllerGrpcIntegrationTest.GrpcPortTestConfiguration.class)
class EventControllerGrpcIntegrationTest {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private GrpcPortHolder grpcPortHolder;

    private Consumer<String, byte[]> consumer;
    private ManagedChannel channel;

    @AfterEach
    void tearDown() {
        if (consumer != null) {
            consumer.close();
        }
        if (channel != null) {
            channel.shutdownNow();
        }
    }

    @Test
    void shouldSaveSensorEventToKafka() throws Exception {
        consumer = createConsumer("sensor-test-group", "telemetry.sensors.v1");

        SensorEventProto request = SensorEventProto.newBuilder()
                .setId("sensor-1")
                .setHubId("hub-1")
                .setTimestamp(com.google.protobuf.Timestamp.newBuilder()
                        .setSeconds(Instant.parse("2024-08-06T15:11:24.157Z").getEpochSecond())
                        .setNanos(157000000)
                        .build())
                .setMotionSensorEvent(MotionSensorProto.newBuilder()
                        .setLinkQuality(75)
                        .setMotion(true)
                        .setVoltage(220)
                        .build())
                .build();

        stub().collectSensorEvent(request);

        ConsumerRecord<String, byte[]> record = pollSingleRecord("telemetry.sensors.v1");
        SensorEventAvro eventAvro = readSensorEvent(record.value());

        assertThat(record.key()).isEqualTo("hub-1");
        assertThat(eventAvro.getId()).isEqualTo("sensor-1");
        assertThat(eventAvro.getHubId()).isEqualTo("hub-1");
    }

    @Test
    void shouldSaveHubEventToKafka() throws Exception {
        consumer = createConsumer("hub-test-group", "telemetry.hubs.v1");

        HubEventProto request = HubEventProto.newBuilder()
                .setHubId("hub-1")
                .setTimestamp(com.google.protobuf.Timestamp.newBuilder()
                        .setSeconds(Instant.parse("2024-08-06T15:11:24.157Z").getEpochSecond())
                        .setNanos(157000000)
                        .build())
                .setDeviceAdded(DeviceAddedProto.newBuilder()
                        .setId("device-1")
                        .setType(DeviceTypeProto.LIGHT_SENSOR)
                        .build())
                .build();

        stub().collectHubEvent(request);

        ConsumerRecord<String, byte[]> record = pollSingleRecord("telemetry.hubs.v1");
        HubEventAvro eventAvro = readHubEvent(record.value());

        assertThat(record.key()).isEqualTo("hub-1");
        assertThat(eventAvro.getHubId()).isEqualTo("hub-1");
    }

    @Test
    void shouldReturnInvalidArgumentWhenSensorPayloadIsMissing() {
        SensorEventProto request = SensorEventProto.newBuilder()
                .setId("sensor-1")
                .setHubId("hub-1")
                .build();

        assertThatThrownBy(() -> stub().collectSensorEvent(request))
                .isInstanceOf(StatusRuntimeException.class)
                .extracting(error -> ((StatusRuntimeException) error).getStatus().getCode())
                .isEqualTo(Status.Code.INVALID_ARGUMENT);
    }

    private CollectorControllerGrpc.CollectorControllerBlockingStub stub() {
        if (channel == null) {
            channel = ManagedChannelBuilder.forAddress("localhost", grpcPortHolder.getPort())
                    .usePlaintext()
                    .build();
        }
        return CollectorControllerGrpc.newBlockingStub(channel);
    }

    private Consumer<String, byte[]> createConsumer(String groupId, String topic) {
        Map<String, Object> props = new HashMap<>(KafkaTestUtils.consumerProps(groupId, "true", embeddedKafkaBroker));
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

    @TestConfiguration
    static class GrpcPortTestConfiguration {

        @Bean
        GrpcPortHolder grpcPortHolder() {
            return new GrpcPortHolder();
        }
    }

    static class GrpcPortHolder implements ApplicationListener<GrpcServerStartedEvent> {

        private volatile int port;

        @Override
        public void onApplicationEvent(GrpcServerStartedEvent event) {
            this.port = event.getPort();
        }

        int getPort() {
            return port;
        }
    }
}
