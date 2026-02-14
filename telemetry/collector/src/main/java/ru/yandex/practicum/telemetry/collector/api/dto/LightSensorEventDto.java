package ru.yandex.practicum.telemetry.collector.api.dto;

import jakarta.validation.constraints.NotNull;

public class LightSensorEventDto extends SensorEventDto {
    @NotNull
    private Integer linkQuality;

    @NotNull
    private Integer luminosity;

    public Integer getLinkQuality() {
        return linkQuality;
    }

    public void setLinkQuality(Integer linkQuality) {
        this.linkQuality = linkQuality;
    }

    public Integer getLuminosity() {
        return luminosity;
    }

    public void setLuminosity(Integer luminosity) {
        this.luminosity = luminosity;
    }
}