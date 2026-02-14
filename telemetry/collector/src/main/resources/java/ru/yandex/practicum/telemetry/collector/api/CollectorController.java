package ru.yandex.practicum.telemetry.collector.api;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.telemetry.collector.api.dto.HubEventDto;
import ru.yandex.practicum.telemetry.collector.api.dto.SensorEventDto;
import ru.yandex.practicum.telemetry.collector.mapper.AvroEventMapper;
import ru.yandex.practicum.telemetry.collector.service.EventProducer;

@RestController
public class CollectorController {
    private final AvroEventMapper mapper;
    private final EventProducer producer;

    public CollectorController(AvroEventMapper mapper, EventProducer producer) {
        this.mapper = mapper;
        this.producer = producer;
    }

    @PostMapping({"/events/sensors", "/events/sensors/"})
    public ResponseEntity<Void> collectSensor(@Valid @RequestBody SensorEventDto event) {
        var avro = mapper.toAvro(event);
        producer.sendSensor(event.getHubId(), avro);
        return ResponseEntity.ok().build();
    }

    @PostMapping({"/events/hubs", "/events/hubs/"})
    public ResponseEntity<Void> collectHub(@Valid @RequestBody HubEventDto event) {
        var avro = mapper.toAvro(event);
        producer.sendHub(event.getHubId(), avro);
        return ResponseEntity.ok().build();
    }
}