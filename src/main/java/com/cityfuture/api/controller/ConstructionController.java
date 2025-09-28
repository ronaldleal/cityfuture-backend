package com.cityfuture.api.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.cityfuture.application.service.ConstructionRequestService;
import com.cityfuture.domain.model.ConstructionOrder;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/constructions")
@RequiredArgsConstructor
public class ConstructionController {
    private final ConstructionRequestService constructionRequestService;

    // Solo el rol ARQUITECTO puede crear solicitudes
    @PreAuthorize("hasRole('ARQUITECTO')")
    @PostMapping
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody ConstructionOrder order) {
        try {
            ConstructionOrder createdOrder = constructionRequestService.createOrder(order);
            Map<String, Object> response = Map.of("idOrden", createdOrder.id(), "message",
                    "La solicitud de construcción se efectuó correctamente", "estado",
                    "Estado actual: ".concat(createdOrder.estado()));
            return ResponseEntity.ok(response);
        } catch (com.cityfuture.domain.exception.LocationAlreadyOccupiedException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Ubicación ocupada", "message", e.getMessage()));
        } catch (com.cityfuture.domain.exception.InsufficientMaterialException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Materiales insuficientes", "message", e.getMessage()));
        }
    }

    // Endpoint para validar antes de crear
    @PreAuthorize("hasRole('ARQUITECTO')")
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateConstructionRequest(
            @RequestBody ConstructionOrder order) {
        Map<String, Object> validation =
                constructionRequestService.validateConstructionRequest(order);

        if ((Boolean) validation.get("valid")) {
            return ResponseEntity.ok(validation);
        } else {
            return ResponseEntity.badRequest().body(validation);
        }
    }

    // Cualquier usuario autenticado puede consultar todas las órdenes
    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<List<ConstructionOrder>> getAllOrders() {
        return ResponseEntity.ok(constructionRequestService.getAllOrders());
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/project-end-date")
    public ResponseEntity<Map<String, Object>> getProjectEndDate() {
        LocalDate endDate = constructionRequestService.getProjectEndDate();
        return ResponseEntity.ok(Map.of("projectEndDate", endDate, "message",
                "Fecha estimada de finalización del proyecto completo"));
    }

    // Cualquier usuario autenticado puede consultar una orden por id
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<ConstructionOrder> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(constructionRequestService.getOrderById(id));
    }

    // Solo ARQUITECTO puede actualizar
    @PreAuthorize("hasRole('ARQUITECTO')")
    @PutMapping("/{id}")
    public ResponseEntity<ConstructionOrder> updateOrder(@PathVariable Long id,
            @RequestBody ConstructionOrder order) {
        return ResponseEntity.ok(constructionRequestService.updateOrder(id, order));
    }

    // Solo ARQUITECTO puede eliminar
    @PreAuthorize("hasRole('ARQUITECTO')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOrder(@PathVariable Long id) {
        try {
            constructionRequestService.deleteOrder(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error",
                    "No se pudo eliminar la orden de construcción", "message", e.getMessage()));
        }
    }

    @PostMapping("/test-scheduler")
    public ResponseEntity<String> testScheduler() {
        constructionRequestService.updateConstructionStatuses();
        return ResponseEntity.ok("Scheduler ejecutado manualmente en fecha: " + LocalDate.now());
    }

    @PostMapping("/process-overdue")
    public ResponseEntity<String> processOverdueOrders() {
        constructionRequestService.processOverdueOrders();
        return ResponseEntity
                .ok("Órdenes atrasadas procesadas correctamente en fecha: " + LocalDate.now());
    }

    @GetMapping("/debug-order/{id}")
    public ResponseEntity<Map<String, Object>> debugOrder(@PathVariable Long id) {
        ConstructionOrder order = constructionRequestService.getOrderById(id);
        LocalDate today = LocalDate.now();
        LocalDate startDate = order.entregaDate().minusDays(order.estimatedDays() - 1);

        Map<String, Object> debug = Map.of("orderId", order.id(), "entregaDate",
                order.entregaDate(), "estimatedDays", order.estimatedDays(), "calculatedStartDate",
                startDate, "today", today, "shouldStart",
                startDate.isBefore(today) || startDate.equals(today), "shouldFinish",
                order.entregaDate().isBefore(today) || order.entregaDate().equals(today),
                "currentStatus", order.estado());

        return ResponseEntity.ok(debug);
    }
}
