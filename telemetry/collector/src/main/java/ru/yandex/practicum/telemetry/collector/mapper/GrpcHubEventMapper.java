package ru.yandex.practicum.telemetry.collector.mapper;

import com.google.protobuf.Timestamp;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.ScenarioConditionProto;
import ru.yandex.practicum.telemetry.collector.model.hub.ActionType;
import ru.yandex.practicum.telemetry.collector.model.hub.ConditionOperation;
import ru.yandex.practicum.telemetry.collector.model.hub.ConditionType;
import ru.yandex.practicum.telemetry.collector.model.hub.DeviceAction;
import ru.yandex.practicum.telemetry.collector.model.hub.DeviceAddedEvent;
import ru.yandex.practicum.telemetry.collector.model.hub.DeviceRemovedEvent;
import ru.yandex.practicum.telemetry.collector.model.hub.DeviceType;
import ru.yandex.practicum.telemetry.collector.model.hub.HubEvent;
import ru.yandex.practicum.telemetry.collector.model.hub.HubEventType;
import ru.yandex.practicum.telemetry.collector.model.hub.ScenarioAddedEvent;
import ru.yandex.practicum.telemetry.collector.model.hub.ScenarioCondition;
import ru.yandex.practicum.telemetry.collector.model.hub.ScenarioRemovedEvent;

@Component
public class GrpcHubEventMapper {

    public HubEvent toDomain(HubEventProto request) {
        HubEvent event = switch (request.getPayloadCase()) {
            case DEVICE_ADDED -> mapDeviceAdded(request);
            case DEVICE_REMOVED -> mapDeviceRemoved(request);
            case SCENARIO_ADDED -> mapScenarioAdded(request);
            case SCENARIO_REMOVED -> mapScenarioRemoved(request);
            case PAYLOAD_NOT_SET -> throw new IllegalArgumentException("Hub payload is not set");
        };

        event.setHubId(request.getHubId());
        event.setTimestamp(toInstant(request.hasTimestamp(), request.getTimestamp()));

        return event;
    }

    private DeviceAddedEvent mapDeviceAdded(HubEventProto request) {
        DeviceAddedEvent event = new DeviceAddedEvent();
        event.setType(HubEventType.DEVICE_ADDED);
        event.setId(request.getDeviceAdded().getId());
        event.setDeviceType(DeviceType.valueOf(request.getDeviceAdded().getType().name()));
        return event;
    }

    private DeviceRemovedEvent mapDeviceRemoved(HubEventProto request) {
        DeviceRemovedEvent event = new DeviceRemovedEvent();
        event.setType(HubEventType.DEVICE_REMOVED);
        event.setId(request.getDeviceRemoved().getId());
        return event;
    }

    private ScenarioAddedEvent mapScenarioAdded(HubEventProto request) {
        ScenarioAddedEvent event = new ScenarioAddedEvent();
        event.setType(HubEventType.SCENARIO_ADDED);
        event.setName(request.getScenarioAdded().getName());
        event.setConditions(mapConditions(request.getScenarioAdded().getConditionsList()));
        event.setActions(mapActions(request.getScenarioAdded().getActionsList()));
        return event;
    }

    private ScenarioRemovedEvent mapScenarioRemoved(HubEventProto request) {
        ScenarioRemovedEvent event = new ScenarioRemovedEvent();
        event.setType(HubEventType.SCENARIO_REMOVED);
        event.setName(request.getScenarioRemoved().getName());
        return event;
    }

    private List<ScenarioCondition> mapConditions(List<ScenarioConditionProto> conditions) {
        return conditions.stream()
                .map(this::mapCondition)
                .toList();
    }

    private ScenarioCondition mapCondition(ScenarioConditionProto conditionProto) {
        ScenarioCondition condition = new ScenarioCondition();
        condition.setSensorId(conditionProto.getSensorId());
        condition.setType(ConditionType.valueOf(conditionProto.getType().name()));
        condition.setOperation(ConditionOperation.valueOf(conditionProto.getOperation().name()));
        if (conditionProto.hasIntValue()) {
            condition.setValue(conditionProto.getIntValue());
        } else if (conditionProto.hasBoolValue()) {
            condition.setValue(conditionProto.getBoolValue());
        }
        return condition;
    }

    private List<DeviceAction> mapActions(List<DeviceActionProto> actions) {
        return actions.stream()
                .map(this::mapAction)
                .toList();
    }

    private DeviceAction mapAction(DeviceActionProto actionProto) {
        DeviceAction action = new DeviceAction();
        action.setSensorId(actionProto.getSensorId());
        action.setType(ActionType.valueOf(actionProto.getType().name()));
        if (actionProto.hasValue()) {
            action.setValue(actionProto.getValue());
        }
        return action;
    }

    private Instant toInstant(boolean hasTimestamp, Timestamp timestamp) {
        return hasTimestamp ? Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos()) : null;
    }
}
