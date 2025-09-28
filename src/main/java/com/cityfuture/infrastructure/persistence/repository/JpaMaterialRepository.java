package com.cityfuture.infrastructure.persistence.repository;

import com.cityfuture.infrastructure.persistence.entity.MaterialEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaMaterialRepository extends JpaRepository<MaterialEntity, Long> {
    Optional<MaterialEntity> findByMaterialName(String materialName);

    Optional<MaterialEntity> findByCode(String code);
}
