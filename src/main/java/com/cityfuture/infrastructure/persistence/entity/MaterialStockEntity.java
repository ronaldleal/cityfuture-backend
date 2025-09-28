package com.cityfuture.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
@Data
@Entity
@Table(name = "material_stock")
public class MaterialStockEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer availableQuantity;
    private String warehouseLocation;

    @OneToOne
    @JoinColumn(name = "material_id")
    private MaterialEntity material;

    private Integer quantity;
}