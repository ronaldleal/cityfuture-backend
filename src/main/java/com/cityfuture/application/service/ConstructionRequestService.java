package com.cityfuture.application.service;

import com.cityfuture.domain.model.ConstructionOrder;
import com.cityfuture.domain.model.ConstructionReport;
import com.cityfuture.domain.model.ProjectSummary;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ConstructionRequestService {
    ConstructionOrder createOrder(ConstructionOrder order);
    List<ConstructionOrder> getAllOrders();
    ConstructionOrder getOrderById(Long id);
    ConstructionOrder updateOrder(Long id, ConstructionOrder order);
    void deleteOrder(Long id);
    Integer getTotalConstructionDays();
    LocalDate getEstimatedDeliveryDate();
    LocalDate getProjectStartDate();
    LocalDate getProjectEndDate();
    ProjectSummary getProjectSummary();
    void updateConstructionStatuses();
    void processOverdueOrders();
    Map<String, Object> validateConstructionRequest(ConstructionOrder order);
    ConstructionReport generateConstructionReport();
}
