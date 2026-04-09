package ru.yandex.practicum.telemetry.analyzer.model;

import java.io.Serializable;
import java.util.Objects;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class ScenarioConditionLinkId implements Serializable {

    @Column(name = "scenario_id")
    private Long scenarioId;

    @Column(name = "sensor_id")
    private String sensorId;

    @Column(name = "condition_id")
    private Long conditionId;

    public Long getScenarioId() {
        return scenarioId;
    }

    public String getSensorId() {
        return sensorId;
    }

    public Long getConditionId() {
        return conditionId;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof ScenarioConditionLinkId other)) {
            return false;
        }
        return Objects.equals(scenarioId, other.scenarioId)
                && Objects.equals(sensorId, other.sensorId)
                && Objects.equals(conditionId, other.conditionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scenarioId, sensorId, conditionId);
    }
}
