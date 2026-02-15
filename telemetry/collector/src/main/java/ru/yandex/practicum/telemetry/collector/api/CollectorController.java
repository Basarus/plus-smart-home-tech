package ru.yandex.practicum.telemetry.collector.api;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.telemetry.collector.api.dto.HubEventDto;
import ru.yandex.practicum.telemetry.collector.api.dto.SensorEventDto;
import ru.yandex.practicum.telemetry.collector.service.CollectorService;

@RestController
@Validated
public class CollectorController {
    private final CollectorService collectorService;

    public CollectorController(CollectorService collectorService) {
        this.collectorService = collectorService;
    }

    @PostMapping("/events/sensors")
    public void collectSensorEvent(@RequestBody SensorEventDto event) {
        collectorService.collectSensorEvent(event);
    }

    @PostMapping("/events/hubs")
    public void collectHubEvent(@RequestBody HubEventDto event) {
        collectorService.collectHubEvent(event);
    }
}