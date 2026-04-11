package ru.yandex.practicum.telemetry.collector.mapper;

import com.google.protobuf.Timestamp;
import java.time.Instant;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.telemetry.collector.model.sensor.ClimateSensorEvent;
import ru.yandex.practicum.telemetry.collector.model.sensor.LightSensorEvent;
import ru.yandex.practicum.telemetry.collector.model.sensor.MotionSensorEvent;
import ru.yandex.practicum.telemetry.collector.model.sensor.SensorEvent;
import ru.yandex.practicum.telemetry.collector.model.sensor.SensorEventType;
import ru.yandex.practicum.telemetry.collector.model.sensor.SwitchSensorEvent;
import ru.yandex.practicum.telemetry.collector.model.sensor.TemperatureSensorEvent;

@Component
public class GrpcSensorEventMapper {

    public SensorEvent toDomain(SensorEventProto request) {
        SensorEvent event = switch (request.getPayloadCase()) {
            case MOTION_SENSOR_EVENT -> mapMotion(request);
            case TEMPERATURE_SENSOR_EVENT -> mapTemperature(request);
            case LIGHT_SENSOR_EVENT -> mapLight(request);
            case CLIMATE_SENSOR_EVENT -> mapClimate(request);
            case SWITCH_SENSOR_EVENT -> mapSwitch(request);
            case PAYLOAD_NOT_SET -> throw new IllegalArgumentException("Sensor payload is not set");
        };

        event.setId(request.getId());
        event.setHubId(request.getHubId());
        event.setTimestamp(toInstant(request.hasTimestamp(), request.getTimestamp()));

        return event;
    }

    private MotionSensorEvent mapMotion(SensorEventProto request) {
        MotionSensorEvent event = new MotionSensorEvent();
        event.setType(SensorEventType.MOTION_SENSOR_EVENT);
        event.setLinkQuality(request.getMotionSensorEvent().getLinkQuality());
        event.setMotion(request.getMotionSensorEvent().getMotion());
        event.setVoltage(request.getMotionSensorEvent().getVoltage());
        return event;
    }

    private TemperatureSensorEvent mapTemperature(SensorEventProto request) {
        TemperatureSensorEvent event = new TemperatureSensorEvent();
        event.setType(SensorEventType.TEMPERATURE_SENSOR_EVENT);
        event.setTemperatureC(request.getTemperatureSensorEvent().getTemperatureC());
        event.setTemperatureF(request.getTemperatureSensorEvent().getTemperatureF());
        return event;
    }

    private LightSensorEvent mapLight(SensorEventProto request) {
        LightSensorEvent event = new LightSensorEvent();
        event.setType(SensorEventType.LIGHT_SENSOR_EVENT);
        event.setLinkQuality(request.getLightSensorEvent().getLinkQuality());
        event.setLuminosity(request.getLightSensorEvent().getLuminosity());
        return event;
    }

    private ClimateSensorEvent mapClimate(SensorEventProto request) {
        ClimateSensorEvent event = new ClimateSensorEvent();
        event.setType(SensorEventType.CLIMATE_SENSOR_EVENT);
        event.setTemperatureC(request.getClimateSensorEvent().getTemperatureC());
        event.setHumidity(request.getClimateSensorEvent().getHumidity());
        event.setCo2Level(request.getClimateSensorEvent().getCo2Level());
        return event;
    }

    private SwitchSensorEvent mapSwitch(SensorEventProto request) {
        SwitchSensorEvent event = new SwitchSensorEvent();
        event.setType(SensorEventType.SWITCH_SENSOR_EVENT);
        event.setState(request.getSwitchSensorEvent().getState());
        return event;
    }

    private Instant toInstant(boolean hasTimestamp, Timestamp timestamp) {
        return hasTimestamp ? Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos()) : null;
    }
}
