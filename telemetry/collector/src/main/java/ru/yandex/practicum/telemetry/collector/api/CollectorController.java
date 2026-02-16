package ru.yandex.practicum.telemetry.collector.api;

import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.telemetry.collector.api.dto.HubEventDto;
import ru.yandex.practicum.telemetry.collector.api.dto.SensorEventDto;
import ru.yandex.practicum.telemetry.collector.service.CollectorService;

@RestController
@Validated
public class CollectorController {
    private final CollectorService service;

    public CollectorController(CollectorService service) {
        this.service = service;
    }

    @PostMapping("/events/sensors")
    public void sensors(@RequestBody @Valid SensorEventDto event) {
        service.collectSensorEvent(event);
    }

    @PostMapping("/events/hubs")
    public void hubs(@RequestBody @Valid HubEventDto event) {
        service.collectHubEvent(event);
    }
}