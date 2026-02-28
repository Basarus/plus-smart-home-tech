package ru.yandex.practicum.telemetry.analyzer.model;

import jakarta.persistence.*;

@Entity
@Table(name = "actions")
public class Action {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;
    private Integer value;

    public Action() {
    }

    public Action(String type, Integer value) {
        this.type = type;
        this.value = value;
    }

    public Long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public Integer getValue() {
        return value;
    }
}