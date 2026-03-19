package ru.yandex.practicum.telemetry.analyzer.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "scenarios",
        uniqueConstraints = @UniqueConstraint(columnNames = {"hub_id", "name"})
)
public class Scenario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hub_id")
    private String hubId;

    @Column(name = "name")
    private String name;

    @OneToMany(mappedBy = "scenario", fetch = FetchType.LAZY)
    private List<ScenarioCondition> conditions = new ArrayList<>();

    @OneToMany(mappedBy = "scenario", fetch = FetchType.LAZY)
    private List<ScenarioAction> actions = new ArrayList<>();

    public Scenario(String hubId, String name) {
        this.hubId = hubId;
        this.name = name;
    }
}