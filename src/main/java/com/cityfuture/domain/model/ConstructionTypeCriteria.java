package com.cityfuture.domain.model;

import java.util.Map;

import lombok.Getter;

@Getter
public enum ConstructionTypeCriteria {
    CASA(Map.of("Ce", 100, "Gr", 50, "Ar", 90, "Ma", 20, "Ad", 100), 3),
    LAGO(Map.of("Ce", 50, "Gr", 60, "Ar", 80, "Ma", 10, "Ad", 20), 2),
    CANCHA_FUTBOL(Map.of("Ce", 20, "Gr", 20, "Ar", 20, "Ma", 20, "Ad", 20), 1),
    EDIFICIO(Map.of("Ce", 200, "Gr", 100, "Ar", 180, "Ma", 40, "Ad", 200), 6),
    GIMNASIO(Map.of("Ce", 50, "Gr", 25, "Ar", 45, "Ma", 10, "Ad", 50), 2);

    private final Map<String, Integer> materials;
    private final Integer estimatedTime;

    ConstructionTypeCriteria(Map<String, Integer> materials, Integer estimatedTime) {
        this.materials = materials;
        this.estimatedTime = estimatedTime;
    }

}