package ru.yandex.practicum.telemetry.aggregator.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aggregator.kafka")
public class AggregatorKafkaProperties {

    private Duration pollTimeout = Duration.ofSeconds(1);
    private final Topics topics = new Topics();

    public Duration getPollTimeout() {
        return pollTimeout;
    }

    public void setPollTimeout(Duration pollTimeout) {
        this.pollTimeout = pollTimeout;
    }

    public Topics getTopics() {
        return topics;
    }

    public static class Topics {
        private String sensors = "telemetry.sensors.v1";
        private String snapshots = "telemetry.snapshots.v1";
        private int partitions = 1;
        private int replicas = 1;

        public String getSensors() {
            return sensors;
        }

        public void setSensors(String sensors) {
            this.sensors = sensors;
        }

        public String getSnapshots() {
            return snapshots;
        }

        public void setSnapshots(String snapshots) {
            this.snapshots = snapshots;
        }

        public int getPartitions() {
            return partitions;
        }

        public void setPartitions(int partitions) {
            this.partitions = partitions;
        }

        public int getReplicas() {
            return replicas;
        }

        public void setReplicas(int replicas) {
            this.replicas = replicas;
        }
    }
}
