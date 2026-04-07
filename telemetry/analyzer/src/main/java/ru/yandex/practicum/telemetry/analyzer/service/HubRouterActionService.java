package ru.yandex.practicum.telemetry.analyzer.service;

import com.google.protobuf.Timestamp;
import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.grpc.telemetry.event.ActionTypeProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;
import ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc.HubRouterControllerBlockingStub;

@Service
public class HubRouterActionService {

    private static final Logger log = LoggerFactory.getLogger(HubRouterActionService.class);

    private final HubRouterControllerBlockingStub hubRouterClient;

    public HubRouterActionService(@GrpcClient("hub-router") HubRouterControllerBlockingStub hubRouterClient) {
        this.hubRouterClient = hubRouterClient;
    }

    // Собираем из рассчитанной команды сценария и отправляю его в роутер устройств.
    public void execute(ScenarioActionCommand command) {
        DeviceActionProto.Builder actionBuilder = DeviceActionProto.newBuilder()
                .setSensorId(command.sensorId())
                .setType(ActionTypeProto.valueOf(command.type().name()));
        if (command.value() != null) {
            actionBuilder.setValue(command.value());
        }

        DeviceActionRequest request = DeviceActionRequest.newBuilder()
                .setHubId(command.hubId())
                .setScenarioName(command.scenarioName())
                .setAction(actionBuilder.build())
                .setTimestamp(toTimestamp(command.timestamp()))
                .build();
        try {
            hubRouterClient.handleDeviceAction(request);
        } catch (StatusRuntimeException exception) {
            log.error("Unable to execute scenario {} for hub {}", command.scenarioName(), command.hubId(), exception);
        }
    }

    private Timestamp toTimestamp(java.time.Instant instant) {
        return Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();
    }
}
