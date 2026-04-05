package ru.yandex.practicum.telemetry.collector.kafka;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "collector.topics")
public class CollectorTopicsProperties {

    private String sensors = "telemetry.sensors.v1";
    private String hubs = "telemetry.hubs.v1";
    private int partitions = 1;
    private short replicas = 1;

    public String getSensors() {
        return sensors;
    }

    public void setSensors(String sensors) {
        this.sensors = sensors;
    }

    public String getHubs() {
        return hubs;
    }

    public void setHubs(String hubs) {
        this.hubs = hubs;
    }

    public int getPartitions() {
        return partitions;
    }

    public void setPartitions(int partitions) {
        this.partitions = partitions;
    }

    public short getReplicas() {
        return replicas;
    }

    public void setReplicas(short replicas) {
        this.replicas = replicas;
    }
}
