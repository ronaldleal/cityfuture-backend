package com.cityfuture.api.controller;

import com.cityfuture.infrastructure.service.ConstructionRequestUseCase;
import com.cityfuture.infrastructure.service.TestSchedulerService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@AllArgsConstructor
public class TestController {

    private static final Logger logger = LoggerFactory.getLogger(TestController.class);

    private final TestSchedulerService testSchedulerService;
    private final ConstructionRequestUseCase constructionService;

    @GetMapping("/scheduler/simulate/{date}")
    public ResponseEntity<?> simulateScheduler(@PathVariable String date) {
        logger.info("Simulación de scheduler solicitada para fecha: {}", date);

        try {
            LocalDate testDate = LocalDate.parse(date);
            String result = testSchedulerService.simulateSchedulerForDate(testDate);
            logger.info("Simulación completada para fecha: {}", date);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error en simulación de scheduler para fecha: {}", date, e);
            return ResponseEntity.status(500).body(Map.of("error", "Error en simulación", "message",
                    "Error al simular el scheduler", "timestamp", LocalDateTime.now()));
        }
    }

    @PostMapping("/scheduler/execute/{date}")
    public ResponseEntity<?> executeScheduler(@PathVariable String date) {
        logger.info("Ejecución de scheduler solicitada para fecha: {}", date);

        try {
            LocalDate testDate = LocalDate.parse(date);
            String result = testSchedulerService.executeSchedulerForDate(testDate);
            logger.info("Scheduler ejecutado exitosamente para fecha: {}", date);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error al ejecutar scheduler para fecha: {}", date, e);
            return ResponseEntity.status(500).body(Map.of("error", "Error en ejecución", "message",
                    "Error al ejecutar el scheduler", "timestamp", LocalDateTime.now()));
        }
    }

    @PostMapping("/scheduler/execute-today")
    public ResponseEntity<?> executeSchedulerToday() {
        logger.info("Ejecución de scheduler solicitada para hoy");

        try {
            constructionService.updateConstructionStatuses();
            String message = "Scheduler ejecutado para la fecha de hoy: " + LocalDate.now();
            logger.info("Scheduler ejecutado exitosamente para hoy");
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            logger.error("Error al ejecutar scheduler para hoy", e);
            return ResponseEntity.status(500).body(Map.of("error", "Error en ejecución", "message",
                    "Error al ejecutar el scheduler para hoy", "timestamp", LocalDateTime.now()));
        }
    }

    @PostMapping("/scheduler/process-overdue")
    public ResponseEntity<String> processOverdueOrders() {
        constructionService.processOverdueOrders();
        return ResponseEntity.ok("Órdenes atrasadas procesadas correctamente");
    }

    @PostMapping("/scheduler/process-range/{startDate}/{endDate}")
    public ResponseEntity<String> processSchedulerRange(@PathVariable String startDate,
            @PathVariable String endDate) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        String result = testSchedulerService.processSchedulerFromDate(start, end);
        return ResponseEntity.ok(result);
    }
}
