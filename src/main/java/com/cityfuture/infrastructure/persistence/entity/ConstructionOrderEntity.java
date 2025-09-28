package com.cityfuture.infrastructure.persistence.entity;

import java.time.LocalDate;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

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
