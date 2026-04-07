package ru.yandex.practicum.telemetry.analyzer.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.kafka.telemetry.event.HubSnapshotAvro;
import ru.yandex.practicum.telemetry.analyzer.model.Scenario;
import ru.yandex.practicum.telemetry.analyzer.repository.ScenarioRepository;

@Service
public class SnapshotService {

    private final ScenarioRepository scenarioRepository;
    private final ScenarioEvaluator scenarioEvaluator;
    private final HubRouterActionService hubRouterActionService;

    public SnapshotService(ScenarioRepository scenarioRepository,
                           ScenarioEvaluator scenarioEvaluator,
                           HubRouterActionService hubRouterActionService) {
        this.scenarioRepository = scenarioRepository;
        this.scenarioEvaluator = scenarioEvaluator;
        this.hubRouterActionService = hubRouterActionService;
    }

    @Transactional(readOnly = true)
    public void handle(HubSnapshotAvro snapshot) {
        List<Scenario> scenarios = scenarioRepository.findByHubId(snapshot.getHubId());
        scenarioEvaluator.evaluate(snapshot, scenarios)
                .forEach(hubRouterActionService::execute);
    }
}
