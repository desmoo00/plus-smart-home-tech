package ru.yandex.practicum.telemetry.analyzer.model;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "scenarios",
        uniqueConstraints = @UniqueConstraint(columnNames = {"hub_id", "name"})
)
public class Scenario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hub_id", nullable = false)
    private String hubId;

    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy = "scenario", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<ScenarioConditionLink> conditions = new LinkedHashSet<>();

    @OneToMany(mappedBy = "scenario", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<ScenarioActionLink> actions = new LinkedHashSet<>();

    protected Scenario() {
    }

    public Scenario(String hubId, String name) {
        this.hubId = hubId;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getHubId() {
        return hubId;
    }

    public String getName() {
        return name;
    }

    public Set<ScenarioConditionLink> getConditions() {
        return conditions;
    }

    public Set<ScenarioActionLink> getActions() {
        return actions;
    }

    public void addCondition(ScenarioConditionLink link) {
        link.attachToScenario(this);
        conditions.add(link);
    }

    public void addAction(ScenarioActionLink link) {
        link.attachToScenario(this);
        actions.add(link);
    }

    public void replaceConditions(Collection<ScenarioConditionLink> newConditions) {
        conditions.clear();
        newConditions.forEach(this::addCondition);
    }

    public void replaceActions(Collection<ScenarioActionLink> newActions) {
        actions.clear();
        newActions.forEach(this::addAction);
    }

    public boolean removeSensorReferences(String sensorId) {
        boolean conditionsChanged = conditions.removeIf(link -> link.getSensor().getId().equals(sensorId));
        boolean actionsChanged = actions.removeIf(link -> link.getSensor().getId().equals(sensorId));
        return conditionsChanged || actionsChanged;
    }
}
