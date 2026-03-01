package ru.yandex.practicum.telemetry.analyzer.service;

import com.google.protobuf.Timestamp;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.grpc.telemetry.message.event.ActionTypeProto;
import ru.yandex.practicum.grpc.telemetry.message.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.message.event.DeviceActionRequest;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.telemetry.analyzer.grpc.HubRouterClient;
import ru.yandex.practicum.telemetry.analyzer.serialization.SpecificAvroDeserializer;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class ScenarioEngine {

    private final SpecificAvroDeserializer avroDeserializer;
    private final ScenarioRuntimeLoader runtimeLoader;
    private final HubRouterClient hubRouterClient;

    public ScenarioEngine(
            SpecificAvroDeserializer avroDeserializer,
            ScenarioRuntimeLoader runtimeLoader,
            HubRouterClient hubRouterClient
    ) {
        this.avroDeserializer = avroDeserializer;
        this.runtimeLoader = runtimeLoader;
        this.hubRouterClient = hubRouterClient;
    }

    public void handleSnapshot(byte[] payload) {
        SensorsSnapshotAvro snapshot = avroDeserializer.deserialize(payload, SensorsSnapshotAvro.class);
        String hubId = snapshot.getHubId() == null ? null : snapshot.getHubId().toString();
        if (hubId == null || hubId.isBlank()) return;

        List<ScenarioRuntime> scenarios = runtimeLoader.loadByHubId(hubId);
        if (scenarios.isEmpty()) return;

        Map<String, SensorStateAvro> sensorsState = snapshot.getSensorsState();

        for (ScenarioRuntime sc : scenarios) {
            if (!conditionsOk(sc.conditions(), sensorsState)) continue;
            for (ScenarioRuntime.ActionCmd action : sc.actions()) {
                DeviceActionRequest req = toRequest(hubId, sc.name(), action);
                if (req != null) {
                    hubRouterClient.handleDeviceAction(req);
                }
            }
        }
    }

    private boolean conditionsOk(List<ScenarioRuntime.ConditionCheck> conditions,
                                 Map<String, SensorStateAvro> sensorsState) {
        for (ScenarioRuntime.ConditionCheck c : conditions) {
            Integer actual = readValue(sensorsState, c.sensorId(), c.type());
            if (actual == null) return false;
            if (!compare(actual, c.operation(), c.value())) return false;
        }
        return true;
    }

    private boolean compare(int actual, String operation, int target) {
        if (operation == null) return false;
        return switch (operation) {
            case "LOWER_THAN" -> actual < target;
            case "GREATER_THAN" -> actual > target;
            case "EQUALS" -> actual == target;
            default -> false;
        };
    }

    private Integer readValue(Map<String, SensorStateAvro> sensorsState,
                              String sensorId,
                              String conditionType) {
        if (sensorsState == null || sensorId == null || conditionType == null) return null;
        SensorStateAvro state = sensorsState.get(sensorId);
        if (state == null) return null;

        Object data = state.getData();

        return switch (conditionType) {
            case "TEMPERATURE" -> readTemperature(data);
            case "HUMIDITY" -> readHumidity(data);
            case "LIGHT" -> readLight(data);
            case "MOTION" -> readMotion(data);
            case "SWITCH" -> readSwitch(data);
            default -> null;
        };
    }

    private Integer readTemperature(Object data) {
        if (data instanceof ClimateSensorAvro c) return c.getTemperatureC();
        if (data instanceof TemperatureSensorAvro t) return t.getTemperatureC();
        return null;
    }

    private Integer readHumidity(Object data) {
        if (data instanceof ClimateSensorAvro c) return c.getHumidity();
        return null;
    }

    private Integer readLight(Object data) {
        if (data instanceof LightSensorAvro l) return l.getLuminosity();
        return null;
    }

    private Integer readMotion(Object data) {
        if (data instanceof MotionSensorAvro m) return m.getMotion() ? 1 : 0;
        return null;
    }

    private Integer readSwitch(Object data) {
        if (data instanceof SwitchSensorAvro s) return s.getState() ? 1 : 0;
        return null;
    }

    private DeviceActionRequest toRequest(String hubId, String scenarioName, ScenarioRuntime.ActionCmd a) {
        if (hubId == null || scenarioName == null || a == null) return null;

        ActionTypeProto type;
        try {
            type = ActionTypeProto.valueOf(a.type());
        } catch (Exception e) {
            type = ActionTypeProto.ACTIVATE;
        }

        DeviceActionProto action = DeviceActionProto.newBuilder()
                .setType(type)
                .setValue(a.value())
                .build();

        Instant now = Instant.now();
        Timestamp ts = Timestamp.newBuilder()
                .setSeconds(now.getEpochSecond())
                .setNanos(now.getNano())
                .build();

        return DeviceActionRequest.newBuilder()
                .setHubId(hubId)
                .setScenarioName(scenarioName)
                .setAction(action)
                .setTimestamp(ts)
                .build();
    }
}