package ru.yandex.practicum.telemetry.analyzer.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "scenario_conditions")
public class ScenarioConditionLink {

    @EmbeddedId
    private ScenarioConditionLinkId id = new ScenarioConditionLinkId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("scenarioId")
    @JoinColumn(name = "scenario_id", nullable = false)
    private Scenario scenario;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("sensorId")
    @JoinColumn(name = "sensor_id", nullable = false)
    private Sensor sensor;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @MapsId("conditionId")
    @JoinColumn(name = "condition_id", nullable = false)
    private Condition condition;

    protected ScenarioConditionLink() {
    }

    public ScenarioConditionLink(Sensor sensor, Condition condition) {
        this.sensor = sensor;
        this.condition = condition;
    }

    void attachToScenario(Scenario scenario) {
        this.scenario = scenario;
    }

    public Sensor getSensor() {
        return sensor;
    }

    public Condition getCondition() {
        return condition;
    }
}
