package com.cityfuture.api.controller;

import com.cityfuture.api.dto.CreateConstructionOrderRequest;
import com.cityfuture.domain.model.ConstructionOrder;
import com.cityfuture.infrastructure.service.ConstructionRequestUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Tag(name = "Construcciones", description = "API para gestión de órdenes de construcción")
@RestController
@RequestMapping("/api/constructions")
@AllArgsConstructor
@Validated
public class ConstructionController {
    private static final Logger logger = LoggerFactory.getLogger(ConstructionController.class);
    private final ConstructionRequestUseCase constructionRequestService;

    @Operation(summary = "Crear nueva orden de construcción", 
               description = "Crea una nueva orden de construcción con validaciones automáticas de ubicación y materiales")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Orden creada exitosamente",
                    content = @Content(mediaType = "application/json", 
                    examples = @ExampleObject(value = "{\"idOrden\": 1, \"message\": \"La solicitud de construcción se efectuó correctamente\", \"estado\": \"Estado actual: Pendiente\"}"))),
        @ApiResponse(responseCode = "400", description = "Error de validación o ubicación ocupada"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping
    public ResponseEntity<Map<String, Object>> createOrder(@Valid @RequestBody CreateConstructionOrderRequest request) {
        logger.info("Solicitud de creación de orden recibida - Proyecto: {}", request.projectName());

        try {
            ConstructionOrder order = request.toDomain();
            ConstructionOrder createdOrder = constructionRequestService.createOrder(order);
            Map<String, Object> response = Map.of("idOrden", createdOrder.id(), "message",
                    "La solicitud de construcción se efectuó correctamente", "estado",
                    "Estado actual: ".concat(createdOrder.estado()));
            logger.info("Orden creada exitosamente - ID: {}", createdOrder.id());
            return ResponseEntity.ok(response);
        } catch (com.cityfuture.domain.exception.LocationAlreadyOccupiedException e) {
            logger.warn("Intento de crear orden en ubicación ocupada - Proyecto: {}",
                    request.projectName());
            return ResponseEntity.badRequest().body(Map.of("error", "Ubicación ocupada", "message",
                    e.getMessage(), "timestamp", LocalDateTime.now()));
        } catch (com.cityfuture.domain.exception.InsufficientMaterialException e) {
            logger.warn("Intento de crear orden sin materiales suficientes - Proyecto: {}",
                    request.projectName());
            return ResponseEntity.badRequest().body(Map.of("error", "Materiales insuficientes",
                    "message", e.getMessage(), "timestamp", LocalDateTime.now()));
        } catch (Exception e) {
            logger.error("Error inesperado al crear orden - Proyecto: {}", request.projectName(), e);
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Error interno del servidor", "message",
                            "Ocurrió un error inesperado al procesar la solicitud", "timestamp",
                            LocalDateTime.now()));
        }
    }

    @PreAuthorize("hasRole('ARQUITECTO')")
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateConstructionRequest(
            @Valid @RequestBody ConstructionOrder order) {
        Map<String, Object> validation =
                constructionRequestService.validateConstructionRequest(order);

        if ((Boolean) validation.get("valid")) {
            return ResponseEntity.ok(validation);
        } else {
            return ResponseEntity.badRequest().body(validation);
        }
    }

    @Operation(summary = "Obtener todas las órdenes de construcción", 
               description = "Obtiene la lista de todas las órdenes de construcción, opcionalmente filtradas por estado")
    @Parameter(name = "estado", description = "Filtrar órdenes por estado (Pendiente, En Progreso, Finalizado)", 
               example = "Pendiente", required = false)
    @GetMapping
    public ResponseEntity<List<ConstructionOrder>> getAllOrders(
            @RequestParam(value = "estado", required = false) String estado) {
        logger.info("Solicitando lista de construcciones con estado: {}", estado != null ? estado : "todos");
        
        if (estado != null && !estado.trim().isEmpty()) {
            return ResponseEntity.ok(constructionRequestService.getOrdersByStatus(estado.trim()));
        } else {
            return ResponseEntity.ok(constructionRequestService.getAllOrders());
        }
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/project-end-date")
    public ResponseEntity<Map<String, Object>> getProjectEndDate() {
        LocalDate endDate = constructionRequestService.getProjectEndDate();
        return ResponseEntity.ok(Map.of("projectEndDate", endDate, "message",
                "Fecha estimada de finalización del proyecto completo"));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable @NotNull @Min(1) Long id) {
        logger.debug("Consultando orden por ID: {}", id);

        try {
            ConstructionOrder order = constructionRequestService.getOrderById(id);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            logger.warn("Orden no encontrada - ID: {}", id);
            return ResponseEntity.status(404)
                    .body(Map.of("error", "Orden no encontrada", "message",
                            "No existe una orden de construcción con el ID: " + id, "timestamp",
                            LocalDateTime.now()));
        } catch (Exception e) {
            logger.error("Error al consultar orden - ID: {}", id, e);
            return ResponseEntity.status(500).body(Map.of("error", "Error interno del servidor",
                    "message", "Error al consultar la orden", "timestamp", LocalDateTime.now()));
        }
    }

    @PreAuthorize("hasRole('ARQUITECTO')")
    @PutMapping("/{id}")
    public ResponseEntity<ConstructionOrder> updateOrder(@PathVariable Long id,
            @RequestBody ConstructionOrder order) {
        return ResponseEntity.ok(constructionRequestService.updateOrder(id, order));
    }

    @PreAuthorize("hasRole('ARQUITECTO')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOrder(@PathVariable @NotNull @Min(1) Long id) {
        logger.info("Solicitud de eliminación de orden - ID: {}", id);

        try {
            constructionRequestService.deleteOrder(id);
            logger.info("Orden eliminada exitosamente - ID: {}", id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            logger.warn("Intento de eliminar orden inexistente - ID: {}", id);
            return ResponseEntity.status(404)
                    .body(Map.of("error", "No se pudo eliminar la orden de construcción", "message",
                            e.getMessage(), "timestamp", LocalDateTime.now()));
        } catch (Exception e) {
            logger.error("Error inesperado al eliminar orden - ID: {}", id, e);
            return ResponseEntity.status(500).body(Map.of("error", "Error interno del servidor",
                    "message", "Error al eliminar la orden", "timestamp", LocalDateTime.now()));
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
