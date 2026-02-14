package ru.yandex.practicum.telemetry.collector.api.dto;

import jakarta.validation.constraints.NotBlank;

public class DeviceAddedEventDto extends HubEventDto {
    @NotBlank
    private String id;

    @NotBlank
    private String deviceType;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }
}