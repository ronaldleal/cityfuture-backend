package com.cityfuture.domain.repository;

import com.cityfuture.infrastructure.persistence.entity.ConstructionOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConstructionOrderRepository extends JpaRepository<ConstructionOrderEntity, Long> {
}