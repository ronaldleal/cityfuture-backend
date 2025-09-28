package com.cityfuture.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "materials")
public class MaterialEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String materialName;
    String code;
    private Integer quantity;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "construction_order_id")
//    private ConstructionOrderEntity constructionOrder;
//
//    @OneToMany
//    @JoinColumn(name = "construction_order_id")
//    private List<MaterialEntity> materials;

}
