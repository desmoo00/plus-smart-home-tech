package ru.yandex.practicum.telemetry.analyzer.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.kafka.telemetry.event.DeviceActionAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceRemovedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioConditionAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioRemovedEventAvro;
import ru.yandex.practicum.telemetry.analyzer.model.Action;
import ru.yandex.practicum.telemetry.analyzer.model.ActionType;
import ru.yandex.practicum.telemetry.analyzer.model.Condition;
import ru.yandex.practicum.telemetry.analyzer.model.ConditionOperation;
import ru.yandex.practicum.telemetry.analyzer.model.ConditionType;
import ru.yandex.practicum.telemetry.analyzer.model.Scenario;
import ru.yandex.practicum.telemetry.analyzer.model.ScenarioActionLink;
import ru.yandex.practicum.telemetry.analyzer.model.ScenarioConditionLink;
import ru.yandex.practicum.telemetry.analyzer.model.Sensor;
import ru.yandex.practicum.telemetry.analyzer.repository.ScenarioRepository;
import ru.yandex.practicum.telemetry.analyzer.repository.SensorRepository;

@Service
public class HubEventService {

    private static final Logger log = LoggerFactory.getLogger(HubEventService.class);

    private final SensorRepository sensorRepository;
    private final ScenarioRepository scenarioRepository;

    public HubEventService(SensorRepository sensorRepository, ScenarioRepository scenarioRepository) {
        this.sensorRepository = sensorRepository;
        this.scenarioRepository = scenarioRepository;
    }

    @Transactional
    public void handle(HubEventAvro event) {
        Object payload = event.getPayload();
        if (payload instanceof DeviceAddedEventAvro deviceAdded) {
            handleDeviceAdded(event.getHubId(), deviceAdded);
            return;
        }
        if (payload instanceof DeviceRemovedEventAvro deviceRemoved) {
            handleDeviceRemoved(event.getHubId(), deviceRemoved);
            return;
        }
        if (payload instanceof ScenarioAddedEventAvro scenarioAdded) {
            handleScenarioAdded(event.getHubId(), scenarioAdded);
            return;
        }
        if (payload instanceof ScenarioRemovedEventAvro scenarioRemoved) {
            handleScenarioRemoved(event.getHubId(), scenarioRemoved);
        }
    }

    private void handleDeviceAdded(String hubId, DeviceAddedEventAvro event) {
        sensorRepository.findByIdAndHubId(event.getId(), hubId)
                .orElseGet(() -> sensorRepository.save(new Sensor(event.getId(), hubId)));
    }

    private void handleDeviceRemoved(String hubId, DeviceRemovedEventAvro event) {
        sensorRepository.findByIdAndHubId(event.getId(), hubId).ifPresent(sensor -> {
            List<Scenario> scenarios = scenarioRepository.findByHubId(hubId);
            for (Scenario scenario : scenarios) {
                if (scenario.removeSensorReferences(sensor.getId())) {
                    if (scenario.getConditions().isEmpty() || scenario.getActions().isEmpty()) {
                        scenarioRepository.delete(scenario);
                    } else {
                        scenarioRepository.save(scenario);
                    }
                }
            }
            sensorRepository.delete(sensor);
        });
    }

    // Новый сценарий сохраняю только когда для всех его условий и действий уже известны нужные датчики.
    private void handleScenarioAdded(String hubId, ScenarioAddedEventAvro event) {
        Set<String> sensorIds = collectSensorIds(event);
        Map<String, Sensor> sensorsById = sensorRepository.findByIdInAndHubId(sensorIds, hubId).stream()
                .collect(Collectors.toMap(Sensor::getId, Function.identity()));

        if (sensorsById.size() != sensorIds.size()) {
            log.warn("Skipping scenario {} for hub {} because some sensors are missing", event.getName(), hubId);
            return;
        }

        Scenario scenario = scenarioRepository.findByHubIdAndName(hubId, event.getName())
                .orElseGet(() -> new Scenario(hubId, event.getName()));

        scenario.replaceConditions(buildConditions(event.getConditions(), sensorsById));
        scenario.replaceActions(buildActions(event.getActions(), sensorsById));
        scenarioRepository.save(scenario);
    }

    private void handleScenarioRemoved(String hubId, ScenarioRemovedEventAvro event) {
        scenarioRepository.findByHubIdAndName(hubId, event.getName())
                .ifPresent(scenarioRepository::delete);
    }

    private Set<String> collectSensorIds(ScenarioAddedEventAvro event) {
        Set<String> sensorIds = new HashSet<>();
        for (ScenarioConditionAvro condition : event.getConditions()) {
            sensorIds.add(condition.getSensorId());
        }
        for (DeviceActionAvro action : event.getActions()) {
            sensorIds.add(action.getSensorId());
        }
        return sensorIds;
    }

    // Здесь из Avro-условий собираю JPA-связки, которые потом будут храниться вместе со сценарием.
    private List<ScenarioConditionLink> buildConditions(List<ScenarioConditionAvro> conditions,
                                                        Map<String, Sensor> sensorsById) {
        List<ScenarioConditionLink> links = new ArrayList<>();
        for (ScenarioConditionAvro condition : conditions) {
            links.add(new ScenarioConditionLink(
                    sensorsById.get(condition.getSensorId()),
                    new Condition(
                            ConditionType.valueOf(condition.getType().name()),
                            ConditionOperation.valueOf(condition.getOperation().name()),
                            normalizeValue(condition.getValue())
                    )
            ));
        }
        return links;
    }

    // Здесь подготавливаю действия сценария в том виде, в котором их потом можно будет отправлять в Hub Router.
    private List<ScenarioActionLink> buildActions(List<DeviceActionAvro> actions, Map<String, Sensor> sensorsById) {
        List<ScenarioActionLink> links = new ArrayList<>();
        for (DeviceActionAvro action : actions) {
            links.add(new ScenarioActionLink(
                    sensorsById.get(action.getSensorId()),
                    new Action(
                            ActionType.valueOf(action.getType().name()),
                            normalizeValue(action.getValue())
                    )
            ));
        }
        return links;
    }

    private Integer normalizeValue(Object value) {
        if (value instanceof Integer integerValue) {
            return integerValue;
        }
        if (value instanceof Boolean booleanValue) {
            return booleanValue ? 1 : 0;
        }
        return null;
    }
}
