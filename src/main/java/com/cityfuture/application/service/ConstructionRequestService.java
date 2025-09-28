package com.cityfuture.application.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import com.cityfuture.domain.model.ConstructionOrder;
import com.cityfuture.domain.model.ConstructionReport;
import com.cityfuture.domain.model.ProjectSummary;

public interface ConstructionRequestService {
    ConstructionOrder createOrder(ConstructionOrder order);

    List<ConstructionOrder> getAllOrders();

    ConstructionOrder getOrderById(Long id);

    ConstructionOrder updateOrder(Long id, ConstructionOrder order);

    void deleteOrder(Long id);

    // Métodos de fechas y estadísticas
    Integer getTotalConstructionDays();

    LocalDate getEstimatedDeliveryDate();

    LocalDate getProjectStartDate();

    LocalDate getProjectEndDate();

    // Método de resumen del proyecto
    ProjectSummary getProjectSummary();

    // Método para actualizar estados (scheduler)
    void updateConstructionStatuses();

    // Método para procesar órdenes atrasadas
    void processOverdueOrders();

    // Método para validar antes de crear
    Map<String, Object> validateConstructionRequest(ConstructionOrder order);

    // Método para generar informe
    ConstructionReport generateConstructionReport();
}
