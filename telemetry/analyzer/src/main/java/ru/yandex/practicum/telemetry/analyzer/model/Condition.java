package ru.yandex.practicum.telemetry.analyzer.model;

import jakarta.persistence.*;

@Entity
@Table(name = "conditions")
public class Condition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;
    private String operation;
    private Integer value;

    public Condition() {
    }

    public Condition(String type, String operation, Integer value) {
        this.type = type;
        this.operation = operation;
        this.value = value;
    }

    public Long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getOperation() {
        return operation;
    }

    public Integer getValue() {
        return value;
    }
}