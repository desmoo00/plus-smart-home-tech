package ru.yandex.practicum.telemetry.analyzer.config;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "analyzer.kafka")
public class AnalyzerKafkaProperties {

    private Duration pollTimeout = Duration.ofSeconds(1);
    private Map<String, String> properties = new HashMap<>();
    private Topics topics = new Topics();
    private Consumers consumers = new Consumers();

    public Duration getPollTimeout() {
        return pollTimeout;
    }

    public void setPollTimeout(Duration pollTimeout) {
        this.pollTimeout = pollTimeout;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public Topics getTopics() {
        return topics;
    }

    public void setTopics(Topics topics) {
        this.topics = topics;
    }

    public Consumers getConsumers() {
        return consumers;
    }

    public void setConsumers(Consumers consumers) {
        this.consumers = consumers;
    }

    public static class Topics {
        private String snapshots = "telemetry.snapshots.v1";
        private String hubs = "telemetry.hubs.v1";

        public String getSnapshots() {
            return snapshots;
        }

        public void setSnapshots(String snapshots) {
            this.snapshots = snapshots;
        }

        public String getHubs() {
            return hubs;
        }

        public void setHubs(String hubs) {
            this.hubs = hubs;
        }
    }

    public static class Consumers {
        private ConsumerSettings snapshots = new ConsumerSettings();
        private ConsumerSettings hubs = new ConsumerSettings();

        public ConsumerSettings getSnapshots() {
            return snapshots;
        }

        public void setSnapshots(ConsumerSettings snapshots) {
            this.snapshots = snapshots;
        }

        public ConsumerSettings getHubs() {
            return hubs;
        }

        public void setHubs(ConsumerSettings hubs) {
            this.hubs = hubs;
        }
    }

    public static class ConsumerSettings {
        private String groupId;
        private Map<String, String> properties = new HashMap<>();

        public String getGroupId() {
            return groupId;
        }

        public void setGroupId(String groupId) {
            this.groupId = groupId;
        }

        public Map<String, String> getProperties() {
            return properties;
        }

        public void setProperties(Map<String, String> properties) {
            this.properties = properties;
        }
    }
}
