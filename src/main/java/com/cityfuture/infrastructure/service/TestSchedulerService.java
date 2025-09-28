package com.cityfuture.infrastructure.service;

import com.cityfuture.infrastructure.persistence.entity.ConstructionOrderEntity;
import com.cityfuture.infrastructure.persistence.repository.JpaConstructionOrderRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class TestSchedulerService {
    private final JpaConstructionOrderRepository orderRepository;

    public TestSchedulerService(JpaConstructionOrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public String simulateSchedulerForDate(LocalDate testDate) {
        StringBuilder result = new StringBuilder();
        result.append("Simulando scheduler para fecha: ").append(testDate).append("\n");

        // Simular inicio de construcciones
        List<ConstructionOrderEntity> pendingOrders = orderRepository.findPendingOrders();
        List<ConstructionOrderEntity> ordersToStart = pendingOrders.stream().filter(order -> {
            LocalDate startDate = order.getEntregaDate().minusDays(order.getEstimatedDays() - 1);
            return startDate.equals(testDate);
        }).toList();

        result.append("Órdenes a iniciar: ").append(ordersToStart.size()).append("\n");
        for (ConstructionOrderEntity order : ordersToStart) {
            result.append("- Orden ").append(order.getId()).append(": ")
                    .append(order.getProjectName()).append("\n");
        }

        // Simular finalización de construcciones
        List<ConstructionOrderEntity> ordersToFinish = orderRepository.findAll().stream()
                .filter(order -> "En progreso".equals(order.getEstado())
                        && order.getEntregaDate().equals(testDate))
                .toList();

        result.append("Órdenes a finalizar: ").append(ordersToFinish.size()).append("\n");
        for (ConstructionOrderEntity order : ordersToFinish) {
            result.append("- Orden ").append(order.getId()).append(": ")
                    .append(order.getProjectName()).append("\n");
        }

        return result.toString();
    }

    public String executeSchedulerForDate(LocalDate testDate) {
        String simulation = simulateSchedulerForDate(testDate);

        // Ejecutar cambios reales
        List<ConstructionOrderEntity> pendingOrders = orderRepository.findPendingOrders();
        List<ConstructionOrderEntity> ordersToStart = pendingOrders.stream().filter(order -> {
            LocalDate startDate = order.getEntregaDate().minusDays(order.getEstimatedDays() - 1);
            return startDate.equals(testDate);
        }).toList();

        ordersToStart.forEach(order -> {
            order.setEstado("En progreso");
            orderRepository.save(order);
        });

        List<ConstructionOrderEntity> ordersToFinish = orderRepository.findAll().stream()
                .filter(order -> "En progreso".equals(order.getEstado())
                        && order.getEntregaDate().equals(testDate))
                .toList();

        ordersToFinish.forEach(order -> {
            order.setEstado("Finalizado");
            orderRepository.save(order);
        });

        return simulation + "\n¡Cambios aplicados!";
    }

    public String processSchedulerFromDate(LocalDate startDate, LocalDate endDate) {
        StringBuilder result = new StringBuilder();
        result.append("Procesando scheduler desde ").append(startDate).append(" hasta ")
                .append(endDate).append("\n\n");

        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            String dayResult = executeSchedulerForDate(currentDate);
            result.append("=== ").append(currentDate).append(" ===\n");
            result.append(dayResult).append("\n\n");
            currentDate = currentDate.plusDays(1);
        }

        return result.toString();
    }
}
