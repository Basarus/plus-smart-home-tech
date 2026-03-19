package ru.yandex.practicum.telemetry.analyzer.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "devices")
public class Device {

    @Id
    @Column(name = "id", nullable = false, length = 64)
    private String id;

    @Column(name = "hub_id", nullable = false, length = 64)
    private String hubId;

    @Column(name = "type", nullable = false, length = 64)
    private String type;
}