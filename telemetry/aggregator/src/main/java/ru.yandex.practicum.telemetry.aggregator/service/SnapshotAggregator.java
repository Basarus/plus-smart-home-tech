package ru.yandex.practicum.telemetry.aggregator.service;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class SnapshotAggregator {

    private final Map<String, SensorsSnapshotAvro> snapshots = new HashMap<>();

    public Optional<SensorsSnapshotAvro> updateState(SensorEventAvro event) {
        if (event == null) return Optional.empty();

        String hubId = asString(event.getHubId());
        String sensorId = asString(event.getId());
        if (hubId == null || hubId.isBlank() || sensorId == null || sensorId.isBlank()) {
            return Optional.empty();
        }

        Instant ts = toInstant(event.getTimestamp());

        SensorsSnapshotAvro snapshot = snapshots.computeIfAbsent(hubId, h -> {
            SensorsSnapshotAvro s = new SensorsSnapshotAvro();
            s.setHubId(h);
            s.setTimestamp(ts);
            s.setSensorsState(new HashMap<>());
            return s;
        });

        Map<String, SensorStateAvro> stateMap = snapshot.getSensorsState();
        SensorStateAvro oldState = stateMap.get(sensorId);

        if (oldState != null) {
            Instant oldTs = oldState.getTimestamp();
            if (oldTs != null && oldTs.isAfter(ts)) return Optional.empty();
            if (payloadEquals(oldState.getData(), event.getPayload())) return Optional.empty();
        }

        SensorStateAvro newState = new SensorStateAvro();
        newState.setTimestamp(ts);
        newState.setData(event.getPayload());

        stateMap.put(sensorId, newState);
        snapshot.setTimestamp(ts);

        return Optional.of(snapshot);
    }

    private static boolean payloadEquals(Object a, Object b) {
        if (a == b) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }

    private static String asString(CharSequence s) {
        return s == null ? null : s.toString();
    }

    private static Instant toInstant(Object ts) {
        if (ts == null) return Instant.now();
        if (ts instanceof Instant i) return i;
        if (ts instanceof Long l) return Instant.ofEpochMilli(l);
        if (ts instanceof Integer i) return Instant.ofEpochMilli(i.longValue());
        if (ts instanceof CharSequence cs) return Instant.ofEpochMilli(Long.parseLong(cs.toString()));
        return Instant.now();
    }
}