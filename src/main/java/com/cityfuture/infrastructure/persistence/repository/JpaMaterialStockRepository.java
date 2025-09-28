package com.cityfuture.infrastructure.persistence.repository;

import com.cityfuture.infrastructure.persistence.entity.MaterialStockEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaMaterialStockRepository extends JpaRepository<MaterialStockEntity, Long> {
    Optional<MaterialStockEntity> findByMaterialId(Long materialId);
}
