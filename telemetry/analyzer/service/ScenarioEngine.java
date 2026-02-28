package ru.yandex.practicum.telemetry.analyzer.service;

import com.google.protobuf.Timestamp;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc;
import ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc.HubRouterControllerBlockingStub;
import ru.yandex.practicum.grpc.telemetry.message.event.HubEventProto;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ScenarioEngine {

    private final HubRouterControllerBlockingStub hubRouterClient;
    private final ScenarioRuntimeLoader runtimeLoader;

    public ScenarioEngine(
            @GrpcClient("hub-router") HubRouterControllerBlockingStub hubRouterClient,
            ScenarioRuntimeLoader runtimeLoader
    ) {
        this.hubRouterClient = hubRouterClient;
        this.runtimeLoader = runtimeLoader;
    }

    public void handleSnapshot(Object snapshotAvro) {
        SnapshotView snapshot = SnapshotView.from(snapshotAvro);
        String hubId = snapshot.hubId();
        Map<String, Integer> sensorValues = snapshot.sensorValues();

        List<ScenarioRuntime> scenarios = runtimeLoader.loadScenarioRuntime(hubId);
        if (scenarios.isEmpty()) return;

        scenarios.stream()
                .filter(sc -> predicatesOk(sc, sensorValues))
                .flatMap(sc -> sc.actions().stream().map(a -> toRequest(hubId, sc.name(), a)))
                .filter(Objects::nonNull)
                .forEach(req -> hubRouterClient.handleDeviceAction(req));
    }

    private boolean predicatesOk(ScenarioRuntime scenario, Map<String, Integer> sensorValues) {
        return scenario.conditions().stream()
                .allMatch(c -> checkCondition(c, sensorValues));
    }

    private boolean checkCondition(ScenarioConditionRuntime c, Map<String, Integer> sensorValues) {
        Integer v = sensorValues.get(c.sensorId());
        if (v == null) return false;

        String op = c.operation();
        int target = c.value();

        return switch (op) {
            case "EQUALS" -> v == target;
            case "GREATER_THAN" -> v > target;
            case "LOWER_THAN" -> v < target;
            default -> false;
        };
    }

    private HubEventProto.DeviceActionRequest toRequest(String hubId, String scenarioName, ScenarioActionRuntime a) {
        HubEventProto.ActionTypeProto type;
        try {
            type = HubEventProto.ActionTypeProto.valueOf(a.type());
        } catch (Exception e) {
            type = HubEventProto.ActionTypeProto.ACTION_TYPE_UNSPECIFIED;
        }

        HubEventProto.DeviceActionProto action = HubEventProto.DeviceActionProto.newBuilder()
                .setType(type)
                .setValue(a.value())
                .build();

        Instant now = Instant.now();
        Timestamp ts = Timestamp.newBuilder()
                .setSeconds(now.getEpochSecond())
                .setNanos(now.getNano())
                .build();

        return HubEventProto.DeviceActionRequest.newBuilder()
                .setHubId(hubId)
                .setScenarioName(scenarioName)
                .setAction(action)
                .setTimestamp(ts)
                .build();
    }

    public record SnapshotView(String hubId, Map<String, Integer> sensorValues) {
        public static SnapshotView from(Object snapshotAvro) {
            String hubId = ReflectionSnapshotReader.readHubId(snapshotAvro);
            Map<String, Integer> values = ReflectionSnapshotReader.readSensorValues(snapshotAvro);
            return new SnapshotView(hubId, values);
        }
    }

    static final class ReflectionSnapshotReader {
        private ReflectionSnapshotReader() {}

        public static String readHubId(Object snapshotAvro) {
            try {
                Object hubId = snapshotAvro.getClass().getMethod("getHubId").invoke(snapshotAvro);
                return hubId == null ? null : hubId.toString();
            } catch (Exception e) {
                throw new IllegalStateException("Cannot read hubId from snapshot avro", e);
            }
        }

        @SuppressWarnings("unchecked")
        public static Map<String, Integer> readSensorValues(Object snapshotAvro) {
            try {
                Object states = snapshotAvro.getClass().getMethod("getStates").invoke(snapshotAvro);
                if (states == null) return Map.of();

                List<Object> list = (List<Object>) states;
                return list.stream()
                        .map(ReflectionSnapshotReader::stateToPair)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toMap(
                                p -> p.sensorId,
                                p -> p.value,
                                (a, b) -> b
                        ));
            } catch (Exception e) {
                throw new IllegalStateException("Cannot read states from snapshot avro", e);
            }
        }

        private static Pair stateToPair(Object state) {
            try {
                Object sensorId = state.getClass().getMethod("getSensorId").invoke(state);
                Object value = state.getClass().getMethod("getValue").invoke(state);
                if (sensorId == null || value == null) return null;
                return new Pair(sensorId.toString(), ((Number) value).intValue());
            } catch (Exception e) {
                return null;
            }
        }

        private static final class Pair {
            final String sensorId;
            final int value;

            Pair(String sensorId, int value) {
                this.sensorId = sensorId;
                this.value = value;
            }
        }
    }
}