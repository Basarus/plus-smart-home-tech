package ru.yandex.practicum.telemetry.analyzer.service;

import com.google.protobuf.Timestamp;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.grpc.telemetry.message.event.ActionTypeProto;
import ru.yandex.practicum.grpc.telemetry.message.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.message.event.DeviceActionRequest;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.telemetry.analyzer.grpc.HubRouterClient;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class ScenarioEngine {

    private final ScenarioRuntimeLoader runtimeLoader;
    private final HubRouterClient hubRouterClient;

    public ScenarioEngine(ScenarioRuntimeLoader runtimeLoader, HubRouterClient hubRouterClient) {
        this.runtimeLoader = runtimeLoader;
        this.hubRouterClient = hubRouterClient;
    }

    public void handleSnapshot(SensorsSnapshotAvro snapshot) {
        String hubId = snapshot.getHubId() == null ? null : snapshot.getHubId().toString();
        if (hubId == null || hubId.isBlank()) return;

        List<ScenarioRuntime> scenarios = runtimeLoader.loadByHubId(hubId);
        if (scenarios.isEmpty()) return;

        for (ScenarioRuntime sc : scenarios) {
            for (ScenarioRuntime.ActionCmd a : sc.actions()) {
                DeviceActionRequest req = toRequest(hubId, sc.name(), a);
                if (req != null) hubRouterClient.handleDeviceAction(req);
            }
        }
    }

    private boolean compare(int actual, ConditionOperationAvro operation, int target) {
        if (operation == null) return false;

        return switch (operation) {
            case LOWER_THAN -> actual < target;
            case GREATER_THAN -> actual > target;
            case EQUALS -> actual == target;
        };
    }

    private Integer readValue(Map<String, SensorStateAvro> sensorsState,
                              String sensorId,
                              ConditionTypeAvro conditionType) {

        if (sensorsState == null || sensorId == null || conditionType == null) return null;

        SensorStateAvro state = sensorsState.get(sensorId);
        if (state == null) {
            for (Map.Entry<String, SensorStateAvro> e : sensorsState.entrySet()) {
                if (e.getKey() != null && sensorId.contentEquals(e.getKey())) {
                    state = e.getValue();
                    break;
                }
            }
        }
        if (state == null) return null;

        Object data = state.getData();

        return switch (conditionType) {
            case TEMPERATURE -> readTemperature(data);
            case HUMIDITY -> readHumidity(data);
            case LUMINOSITY -> readLight(data);
            case MOTION -> readMotion(data);
            case SWITCH -> readSwitch(data);
            case CO2LEVEL -> readCo2(data);
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

    private Integer readCo2(Object data) {
        if (data instanceof ClimateSensorAvro c) return c.getCo2Level();
        return null;
    }

    private Integer readMotion(Object data) {
        if (data instanceof MotionSensorAvro m) return Boolean.TRUE.equals(m.getMotion()) ? 1 : 0;
        return null;
    }

    private Integer readSwitch(Object data) {
        if (data instanceof SwitchSensorAvro s) return Boolean.TRUE.equals(s.getState()) ? 1 : 0;
        return null;
    }

    private DeviceActionRequest toRequest(String hubId, String scenarioName, ScenarioRuntime.ActionCmd a) {
        if (hubId == null || scenarioName == null || a == null) return null;
        if (a.type() == null) return null;

        ActionTypeProto type = switch (a.type()) {
            case ActionTypeAvro.ACTIVATE -> ActionTypeProto.ACTIVATE;
            case ActionTypeAvro.DEACTIVATE -> ActionTypeProto.DEACTIVATE;
            case ActionTypeAvro.INVERSE -> ActionTypeProto.INVERSE;
            case ActionTypeAvro.SET_VALUE -> ActionTypeProto.SET_VALUE;
        };

        int value = a.value() == null ? 0 : a.value();

        DeviceActionProto action = DeviceActionProto.newBuilder()
                .setSensorId(a.deviceId())
                .setType(type)
                .setValue(value)
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