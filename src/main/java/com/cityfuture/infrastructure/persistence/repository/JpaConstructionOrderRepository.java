package com.cityfuture.infrastructure.persistence.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.cityfuture.infrastructure.persistence.entity.ConstructionOrderEntity;

@Repository
public interface JpaConstructionOrderRepository
        extends JpaRepository<ConstructionOrderEntity, Long> {

    Optional<ConstructionOrderEntity> findFirstByOrderByEntregaDateAsc();

    Optional<ConstructionOrderEntity> findFirstByOrderByEntregaDateDesc();

    @Query("SELECT COALESCE(SUM(c.estimatedDays), 0) FROM ConstructionOrderEntity c")
    Integer sumAllEstimatedDays();

    @Query("SELECT c FROM ConstructionOrderEntity c WHERE c.estado = 'Pendiente' AND c.startDate = :today")
    List<ConstructionOrderEntity> findOrdersToStartToday(@Param("today") LocalDate today);

    @Query("SELECT c FROM ConstructionOrderEntity c WHERE c.estado = 'En progreso' AND c.entregaDate = :today")
    List<ConstructionOrderEntity> findOrdersToFinishToday(@Param("today") LocalDate today);

    @Query("SELECT c FROM ConstructionOrderEntity c WHERE c.estado = 'Pendiente'")
    List<ConstructionOrderEntity> findPendingOrders();

    @Query("SELECT COUNT(c) > 0 FROM ConstructionOrderEntity c WHERE "
            + "c.location.latitude = :latitude AND c.location.longitude = :longitude")
    boolean existsByLocationCoordinates(@Param("latitude") Double latitude,
            @Param("longitude") Double longitude);
}
