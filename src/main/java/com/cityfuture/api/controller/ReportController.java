package com.cityfuture.api.controller;

import com.cityfuture.domain.model.ConstructionReport;
import com.cityfuture.domain.model.ProjectSummary;
import com.cityfuture.infrastructure.service.ConstructionRequestUseCase;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@AllArgsConstructor
public class ReportController {
    private static final Logger logger = LoggerFactory.getLogger(ReportController.class);

    private final ConstructionRequestUseCase constructionRequestService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/constructions")
    public ResponseEntity<?> getConstructionReport() {
        logger.info("Solicitud de reporte de construcciones");

        try {
            ConstructionReport report = constructionRequestService.generateConstructionReport();
            logger.info(
                    "Reporte de construcciones generado exitosamente - Total órdenes: {}, Pendientes: {}, En progreso: {}, Finalizadas: {}",
                    report.totalOrders(), report.pendingOrders(), report.inProgressOrders(),
                    report.finishedOrders());
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            logger.error("Error al generar reporte de construcciones", e);
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Error interno del servidor", "message",
                            "Error al generar el reporte de construcciones", "timestamp",
                            LocalDateTime.now()));
        }
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/project-summary")
    public ResponseEntity<?> getProjectSummary() {
        logger.info("Solicitud de resumen del proyecto");

        try {
            ProjectSummary summary = constructionRequestService.getProjectSummary();
            logger.info("Resumen del proyecto generado - Total días: {}, Estado: {}",
                    summary.totalConstructionDays(), summary.status());
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            logger.error("Error al generar resumen del proyecto", e);
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Error interno del servidor", "message",
                            "Error al generar el resumen del proyecto", "timestamp",
                            LocalDateTime.now()));
        }
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/project-end-date")
    public ResponseEntity<?> getProjectEndDate() {
        logger.info("Solicitud de fecha de finalización del proyecto");

        try {
            LocalDate endDate = constructionRequestService.getProjectEndDate();
            logger.info("Fecha de finalización calculada: {}", endDate);
            return ResponseEntity.ok(Map.of("projectEndDate", endDate, "message",
                    "Fecha estimada de finalización del proyecto completo"));
        } catch (Exception e) {
            logger.error("Error al calcular fecha de finalización del proyecto", e);
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Error interno del servidor", "message",
                            "Error al calcular la fecha de finalización", "timestamp",
                            LocalDateTime.now()));
        }
    }
}
