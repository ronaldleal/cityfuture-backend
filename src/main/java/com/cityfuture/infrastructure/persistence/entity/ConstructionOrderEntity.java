package com.cityfuture.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "construction_orders")
public class ConstructionOrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String projectName;

    @Embedded
    private CoordinateEmbeddable location;
    private String typeConstruction;
    private String estado;
    private Integer estimatedDays;
    private LocalDate startDate;
    private LocalDate entregaDate;
}
