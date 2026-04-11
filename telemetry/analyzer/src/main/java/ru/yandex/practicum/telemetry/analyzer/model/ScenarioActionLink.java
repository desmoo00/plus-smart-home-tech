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
@Table(name = "scenario_actions")
public class ScenarioActionLink {

    @EmbeddedId
    private ScenarioActionLinkId id = new ScenarioActionLinkId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("scenarioId")
    @JoinColumn(name = "scenario_id", nullable = false)
    private Scenario scenario;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("sensorId")
    @JoinColumn(name = "sensor_id", nullable = false)
    private Sensor sensor;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @MapsId("actionId")
    @JoinColumn(name = "action_id", nullable = false)
    private Action action;

    protected ScenarioActionLink() {
    }

    public ScenarioActionLink(Sensor sensor, Action action) {
        this.sensor = sensor;
        this.action = action;
    }

    void attachToScenario(Scenario scenario) {
        this.scenario = scenario;
    }

    public Sensor getSensor() {
        return sensor;
    }

    public Action getAction() {
        return action;
    }
}
