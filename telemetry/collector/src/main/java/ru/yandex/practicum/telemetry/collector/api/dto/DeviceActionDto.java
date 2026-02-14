package ru.yandex.practicum.telemetry.collector.api.dto;

import jakarta.validation.constraints.NotBlank;

public class DeviceActionDto {
    @NotBlank
    private String sensorId;

    @NotBlank
    private String type;

    private Integer value;

    public String getSensorId() {
        return sensorId;
    }

    public void setSensorId(String sensorId) {
        this.sensorId = sensorId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }
}