package ru.yandex.practicum.telemetry.aggregator.service;

import org.apache.avro.util.Utf8;
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
        String hubId = asString(event.getHubId());
        String sensorId = asString(event.getId());
        long ts = toMillis(event.getTimestamp());

        SensorsSnapshotAvro snapshot = snapshots.computeIfAbsent(hubId, h -> {
            SensorsSnapshotAvro s = new SensorsSnapshotAvro();
            s.setHubId(h);
            s.setTimestamp(ts);
            s.setSensorsState(new HashMap<>());
            return s;
        });

        Map<CharSequence, SensorStateAvro> stateMap = snapshot.getSensorsState();
        SensorStateAvro oldState = stateMap.get(new Utf8(sensorId));

        if (oldState != null) {
            long oldTs = oldState.getTimestamp();
            if (oldTs > ts) return Optional.empty();
            if (payloadEquals(oldState.getData(), event.getPayload())) return Optional.empty();
        }

        SensorStateAvro newState = new SensorStateAvro();
        newState.setTimestamp(ts);
        newState.setData(event.getPayload());

        stateMap.put(new Utf8(sensorId), newState);
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

    private static long toMillis(Object ts) {
        if (ts == null) return Instant.now().toEpochMilli();
        if (ts instanceof Long l) return l;
        if (ts instanceof Integer i) return i.longValue();
        if (ts instanceof CharSequence cs) return Long.parseLong(cs.toString());
        return Instant.now().toEpochMilli();
    }
}