package ru.yandex.practicum.telemetry.collector.api.dto;

import jakarta.validation.constraints.NotNull;

public class SwitchSensorEventDto extends SensorEventDto {
    @NotNull
    private Boolean state;

    public Boolean getState() {
        return state;
    }

    public void setState(Boolean state) {
        this.state = state;
    }
}