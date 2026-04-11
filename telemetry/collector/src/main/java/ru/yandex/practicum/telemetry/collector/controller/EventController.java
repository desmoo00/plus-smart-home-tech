package ru.yandex.practicum.telemetry.collector.controller;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.Set;
import java.util.stream.Collectors;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.yandex.practicum.grpc.telemetry.collector.CollectorControllerGrpc;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.telemetry.collector.mapper.GrpcHubEventMapper;
import ru.yandex.practicum.telemetry.collector.mapper.GrpcSensorEventMapper;
import ru.yandex.practicum.telemetry.collector.model.hub.HubEvent;
import ru.yandex.practicum.telemetry.collector.model.sensor.SensorEvent;
import ru.yandex.practicum.telemetry.collector.service.CollectorService;

@GrpcService
public class EventController extends CollectorControllerGrpc.CollectorControllerImplBase {

    private final CollectorService collectorService;
    private final GrpcSensorEventMapper grpcSensorEventMapper;
    private final GrpcHubEventMapper grpcHubEventMapper;
    private final Validator validator;

    public EventController(CollectorService collectorService,
                           GrpcSensorEventMapper grpcSensorEventMapper,
                           GrpcHubEventMapper grpcHubEventMapper,
                           Validator validator) {
        this.collectorService = collectorService;
        this.grpcSensorEventMapper = grpcSensorEventMapper;
        this.grpcHubEventMapper = grpcHubEventMapper;
        this.validator = validator;
    }

    @Override
    public void collectSensorEvent(SensorEventProto request, StreamObserver<Empty> responseObserver) {
        handle(responseObserver, () -> {
            SensorEvent event = grpcSensorEventMapper.toDomain(request);
            validate(event);
            collectorService.saveSensorEvent(event);
        });
    }

    @Override
    public void collectHubEvent(HubEventProto request, StreamObserver<Empty> responseObserver) {
        handle(responseObserver, () -> {
            HubEvent event = grpcHubEventMapper.toDomain(request);
            validate(event);
            collectorService.saveHubEvent(event);
        });
    }

    private void handle(StreamObserver<Empty> responseObserver, Runnable action) {
        try {
            action.run();
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (IllegalArgumentException exception) {
            responseObserver.onError(new StatusRuntimeException(
                    Status.INVALID_ARGUMENT.withDescription(exception.getMessage()).withCause(exception)
            ));
        } catch (Exception exception) {
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL.withDescription(exception.getMessage()).withCause(exception)
            ));
        }
    }

    private <T> void validate(T target) {
        Set<ConstraintViolation<T>> violations = validator.validate(target);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(violation -> violation.getPropertyPath() + " " + violation.getMessage())
                    .collect(Collectors.joining("; "));
            throw new IllegalArgumentException(message);
        }
    }
}
