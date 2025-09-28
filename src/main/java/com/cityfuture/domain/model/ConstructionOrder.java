package com.cityfuture.domain.model;

import java.time.LocalDate;

public record ConstructionOrder(Long id, String projectName, Coordinate location,
                String typeConstruction, String estado, Integer estimatedDays,
                LocalDate entregaDate) {
}
