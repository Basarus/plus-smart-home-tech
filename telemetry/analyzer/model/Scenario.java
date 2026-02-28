package ru.yandex.practicum.telemetry.analyzer.model;

import jakarta.persistence.*;

@Entity
@Table(name = "scenarios", uniqueConstraints = @UniqueConstraint(columnNames = {"hub_id", "name"}))
public class Scenario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hub_id")
    private String hubId;

    @Column(name = "name")
    private String name;

    public Scenario() {
    }

    public Scenario(String hubId, String name) {
        this.hubId = hubId;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getHubId() {
        return hubId;
    }

    public void setHubId(String hubId) {
        this.hubId = hubId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}