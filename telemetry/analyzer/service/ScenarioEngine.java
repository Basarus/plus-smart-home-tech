package ru.yandex.practicum.telemetry.analyzer.service;

import com.google.protobuf.Timestamp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.telemetry.analyzer.grpc.HubRouterClient;
import ru.yandex.practicum.telemetry.analyzer.model.Scenario;
import ru.yandex.practicum.telemetry.analyzer.repo.ScenarioRepository;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.*;
import java.util.function.BiPredicate;

@Service
public class ScenarioEngine {
    private final ScenarioRepository scenarioRepository;
    private final HubRouterClient hubRouterClient;

    public ScenarioEngine(ScenarioRepository scenarioRepository, HubRouterClient hubRouterClient) {
        this.scenarioRepository = scenarioRepository;
        this.hubRouterClient = hubRouterClient;
    }

    @Transactional(readOnly = true)
    public void handleSnapshot(Object snapshot) {
        String hubId = readString(snapshot, "getHubId");
        if (hubId == null) {
            return;
        }

        Instant ts = readInstant(snapshot, "getTimestamp");
        if (ts == null) {
            ts = Instant.now();
        }

        Map<String, Object> sensorStates = extractSensorStates(snapshot);

        List<Scenario> scenarios = scenarioRepository.findByHubId(hubId);
        if (scenarios == null || scenarios.isEmpty()) {
            return;
        }

        for (Scenario scenario : scenarios) {
            ScenarioRuntime rt = ScenarioRuntimeLoader.loadScenarioRuntime(scenario);
            if (rt == null) {
                continue;
            }
            if (rt.conditions().isEmpty()) {
                continue;
            }
            boolean ok = rt.conditions().stream().allMatch(c -> evaluateCondition(c, sensorStates));
            if (!ok) {
                continue;
            }

            for (ScenarioRuntime.ActionCmd a : rt.actions()) {
                HubEventProto.DeviceActionProto.Builder actionBuilder = HubEventProto.DeviceActionProto.newBuilder()
                        .setSensorId(a.sensorId())
                        .setType(a.type());

                if (a.value() != null) {
                    actionBuilder.setIntValue(a.value());
                }

                HubEventProto.DeviceActionRequest request = HubEventProto.DeviceActionRequest.newBuilder()
                        .setHubId(hubId)
                        .setScenarioName(scenario.getName())
                        .setAction(actionBuilder.build())
                        .setTimestamp(Timestamp.newBuilder().setSeconds(ts.getEpochSecond()).setNanos(ts.getNano()).build())
                        .build();

                hubRouterClient.handleDeviceAction(request);
            }
        }
    }

    private boolean evaluateCondition(ScenarioRuntime.ConditionCheck c, Map<String, Object> sensorStates) {
        Object state = sensorStates.get(c.sensorId());
        if (state == null) {
            return false;
        }

        String type = normalize(c.type());
        Integer threshold = c.value();
        String op = normalize(c.operation());

        if ("MOTION".equals(type) || "SWITCH".equals(type)) {
            Boolean b = extractBoolean(state, type);
            if (b == null || threshold == null) {
                return false;
            }
            int v = b ? 1 : 0;
            return compareInt(v, threshold, op);
        }

        Integer v = extractInt(state, type);
        if (v == null || threshold == null) {
            return false;
        }
        return compareInt(v, threshold, op);
    }

    private boolean compareInt(int actual, int expected, String op) {
        BiPredicate<Integer, Integer> p = switch (op) {
            case "GREATER", "GREATER_THAN", "GT", "MORE", "BIGGER" -> (a, e) -> a > e;
            case "LESS", "LESS_THAN", "LT", "LOWER", "SMALLER" -> (a, e) -> a < e;
            case "EQUAL", "EQUALS", "EQ" -> Objects::equals;
            default -> Objects::equals;
        };
        return p.test(actual, expected);
    }

    private Integer extractInt(Object state, String type) {
        if ("TEMPERATURE".equals(type)) {
            Integer t = readInt(state, "getTemperatureC");
            if (t != null) return t;
            return readInt(state, "getTemperature");
        }
        if ("HUMIDITY".equals(type)) {
            return readInt(state, "getHumidity");
        }
        if ("CO2LEVEL".equals(type) || "CO2_LEVEL".equals(type) || "CO2".equals(type)) {
            Integer v = readInt(state, "getCo2Level");
            if (v != null) return v;
            return readInt(state, "getCO2Level");
        }
        if ("LUMINOSITY".equals(type) || "LIGHT".equals(type)) {
            return readInt(state, "getLuminosity");
        }
        return readInt(state, "getValue");
    }

    private Boolean extractBoolean(Object state, String type) {
        if ("MOTION".equals(type)) {
            return readBool(state, "getMotion");
        }
        if ("SWITCH".equals(type)) {
            Boolean b = readBool(state, "getState");
            if (b != null) return b;
            return readBool(state, "isState");
        }
        return null;
    }

    private Map<String, Object> extractSensorStates(Object snapshot) {
        Map<String, Object> out = new HashMap<>();

        Object m = invoke(snapshot, "getSensorsState");
        if (m instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> e : map.entrySet()) {
                if (e.getKey() != null && e.getValue() != null) {
                    out.put(e.getKey().toString(), e.getValue());
                }
            }
            return out;
        }

        Object list = invoke(snapshot, "getSensors");
        if (list instanceof Iterable<?> it) {
            for (Object item : it) {
                String id = readString(item, "getId");
                Object state = invoke(item, "getState");
                if (id != null && state != null) {
                    out.put(id, state);
                }
            }
        }

        return out;
    }

    private static String normalize(String s) {
        if (s == null) return "";
        return s.trim().toUpperCase(Locale.ROOT);
    }

    private static Object invoke(Object o, String method) {
        if (o == null) return null;
        try {
            Method m = o.getClass().getMethod(method);
            return m.invoke(o);
        } catch (Exception e) {
            return null;
        }
    }

    private static String readString(Object o, String method) {
        Object v = invoke(o, method);
        return v == null ? null : v.toString();
    }

    private static Integer readInt(Object o, String method) {
        Object v = invoke(o, method);
        if (v instanceof Integer i) return i;
        if (v instanceof Number n) return n.intValue();
        return null;
    }

    private static Boolean readBool(Object o, String method) {
        Object v = invoke(o, method);
        if (v instanceof Boolean b) return b;
        return null;
    }

    private static Instant readInstant(Object o, String method) {
        Object v = invoke(o, method);
        if (v instanceof Instant i) return i;
        if (v instanceof Long l) return Instant.ofEpochMilli(l);
        if (v instanceof Number n) return Instant.ofEpochMilli(n.longValue());
        return null;
    }
}