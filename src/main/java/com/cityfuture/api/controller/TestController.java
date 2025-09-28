package com.cityfuture.api.controller;

import java.time.LocalDate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.cityfuture.infrastructure.service.ConstructionRequestServiceImpl;
import com.cityfuture.infrastructure.service.TestSchedulerService;

@RestController
@RequestMapping("/api/test")
public class TestController {

    private final TestSchedulerService testSchedulerService;
    private final ConstructionRequestServiceImpl constructionService;

    public TestController(TestSchedulerService testSchedulerService,
            ConstructionRequestServiceImpl constructionService) {
        this.testSchedulerService = testSchedulerService;
        this.constructionService = constructionService;
    }

    @GetMapping("/scheduler/simulate/{date}")
    public ResponseEntity<String> simulateScheduler(@PathVariable String date) {
        LocalDate testDate = LocalDate.parse(date);
        String result = testSchedulerService.simulateSchedulerForDate(testDate);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/scheduler/execute/{date}")
    public ResponseEntity<String> executeScheduler(@PathVariable String date) {
        LocalDate testDate = LocalDate.parse(date);
        String result = testSchedulerService.executeSchedulerForDate(testDate);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/scheduler/execute-today")
    public ResponseEntity<String> executeSchedulerToday() {
        constructionService.updateConstructionStatuses();
        return ResponseEntity.ok("Scheduler ejecutado para la fecha de hoy: " + LocalDate.now());
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
