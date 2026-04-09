package ru.yandex.practicum.telemetry.collector.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.grpc.telemetry.event.ActionTypeProto;
import ru.yandex.practicum.grpc.telemetry.event.ConditionOperationProto;
import ru.yandex.practicum.grpc.telemetry.event.ConditionTypeProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceAddedProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceTypeProto;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.ScenarioAddedProto;
import ru.yandex.practicum.grpc.telemetry.event.ScenarioConditionProto;
import ru.yandex.practicum.telemetry.collector.model.hub.ActionType;
import ru.yandex.practicum.telemetry.collector.model.hub.ConditionOperation;
import ru.yandex.practicum.telemetry.collector.model.hub.ConditionType;
import ru.yandex.practicum.telemetry.collector.model.hub.DeviceAddedEvent;
import ru.yandex.practicum.telemetry.collector.model.hub.DeviceType;
import ru.yandex.practicum.telemetry.collector.model.hub.ScenarioAddedEvent;

class GrpcHubEventMapperTest {

    private final GrpcHubEventMapper mapper = new GrpcHubEventMapper();

    @Test
    void shouldMapDeviceAddedPayload() {
        HubEventProto request = HubEventProto.newBuilder()
                .setHubId("hub-1")
                .setDeviceAdded(DeviceAddedProto.newBuilder()
                        .setId("device-1")
                        .setType(DeviceTypeProto.LIGHT_SENSOR)
                        .build())
                .build();

        DeviceAddedEvent event = assertInstanceOf(DeviceAddedEvent.class, mapper.toDomain(request));

        assertEquals("hub-1", event.getHubId());
        assertEquals("device-1", event.getId());
        assertEquals(DeviceType.LIGHT_SENSOR, event.getDeviceType());
    }

    @Test
    void shouldMapScenarioAddedPayload() {
        HubEventProto request = HubEventProto.newBuilder()
                .setHubId("hub-1")
                .setScenarioAdded(ScenarioAddedProto.newBuilder()
                        .setName("warm-room")
                        .addConditions(ScenarioConditionProto.newBuilder()
                                .setSensorId("sensor-1")
                                .setType(ConditionTypeProto.TEMPERATURE)
                                .setOperation(ConditionOperationProto.GREATER_THAN)
                                .setIntValue(25)
                                .build())
                        .addConditions(ScenarioConditionProto.newBuilder()
                                .setSensorId("sensor-2")
                                .setType(ConditionTypeProto.SWITCH)
                                .setOperation(ConditionOperationProto.EQUALS)
                                .setBoolValue(true)
                                .build())
                        .addActions(DeviceActionProto.newBuilder()
                                .setSensorId("device-1")
                                .setType(ActionTypeProto.SET_VALUE)
                                .setValue(1)
                                .build())
                        .addActions(DeviceActionProto.newBuilder()
                                .setSensorId("device-2")
                                .setType(ActionTypeProto.ACTIVATE)
                                .build())
                        .build())
                .build();

        ScenarioAddedEvent event = assertInstanceOf(ScenarioAddedEvent.class, mapper.toDomain(request));

        assertEquals("warm-room", event.getName());
        assertEquals(2, event.getConditions().size());
        assertEquals(2, event.getActions().size());
        assertEquals(ConditionType.TEMPERATURE, event.getConditions().get(0).getType());
        assertEquals(ConditionOperation.GREATER_THAN, event.getConditions().get(0).getOperation());
        assertEquals(25, event.getConditions().get(0).getValue());
        assertEquals(ConditionType.SWITCH, event.getConditions().get(1).getType());
        assertEquals(Boolean.TRUE, event.getConditions().get(1).getValue());
        assertEquals(ActionType.SET_VALUE, event.getActions().get(0).getType());
        assertEquals(1, event.getActions().get(0).getValue());
        assertEquals(ActionType.ACTIVATE, event.getActions().get(1).getType());
        assertFalse(event.getActions().get(1).getType() == ActionType.SET_VALUE);
        assertTrue(event.getActions().get(1).getValue() == null);
    }

    @Test
    void shouldRejectUnsupportedProtoEnums() {
        HubEventProto deviceAdded = HubEventProto.newBuilder()
                .setHubId("hub-1")
                .setDeviceAdded(DeviceAddedProto.newBuilder()
                        .setId("device-1")
                        .setType(DeviceTypeProto.DEVICE_TYPE_PROTO_UNSPECIFIED)
                        .build())
                .build();

        HubEventProto scenarioAdded = HubEventProto.newBuilder()
                .setHubId("hub-1")
                .setScenarioAdded(ScenarioAddedProto.newBuilder()
                        .setName("broken-scenario")
                        .addConditions(ScenarioConditionProto.newBuilder()
                                .setSensorId("sensor-1")
                                .setType(ConditionTypeProto.CONDITION_TYPE_PROTO_UNSPECIFIED)
                                .setOperation(ConditionOperationProto.CONDITION_OPERATION_PROTO_UNSPECIFIED)
                                .setIntValue(1)
                                .build())
                        .addActions(DeviceActionProto.newBuilder()
                                .setSensorId("device-1")
                                .setType(ActionTypeProto.ACTION_TYPE_PROTO_UNSPECIFIED)
                                .build())
                        .build())
                .build();

        IllegalArgumentException deviceTypeError =
                assertThrows(IllegalArgumentException.class, () -> mapper.toDomain(deviceAdded));
        IllegalArgumentException conditionError =
                assertThrows(IllegalArgumentException.class, () -> mapper.toDomain(scenarioAdded));

        assertTrue(deviceTypeError.getMessage().contains("Unsupported device type"));
        assertTrue(conditionError.getMessage().contains("Unsupported condition type"));
    }
}
