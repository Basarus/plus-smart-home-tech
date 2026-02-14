package ru.yandex.practicum.telemetry.collector.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.telemetry.collector.api.dto.HubEventDto;
import ru.yandex.practicum.telemetry.collector.api.dto.SensorEventDto;
import ru.yandex.practicum.telemetry.collector.service.CollectorService;

@RestController
public class CollectorController {
    private final CollectorService collectorService;

    public CollectorController(CollectorService collectorService) {
        this.collectorService = collectorService;
    }

    @PostMapping("/events/sensors")
    public ResponseEntity<Void> collectSensorEvent(@RequestBody SensorEventDto event) {
        collectorService.collectSensorEvent(event);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/events/hubs")
    public ResponseEntity<Void> collectHubEvent(@RequestBody HubEventDto event) {
        collectorService.collectHubEvent(event);
        return ResponseEntity.ok().build();
    }
}