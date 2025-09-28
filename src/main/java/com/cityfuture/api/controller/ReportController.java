package com.cityfuture.api.controller;

import java.time.LocalDate;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.cityfuture.application.service.ConstructionRequestService;
import com.cityfuture.domain.model.ConstructionReport;
import com.cityfuture.domain.model.ProjectSummary;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ConstructionRequestService constructionRequestService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/constructions")
    public ResponseEntity<ConstructionReport> getConstructionReport() {
        ConstructionReport report = constructionRequestService.generateConstructionReport();
        return ResponseEntity.ok(report);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/project-summary")
    public ResponseEntity<ProjectSummary> getProjectSummary() {
        ProjectSummary summary = constructionRequestService.getProjectSummary();
        return ResponseEntity.ok(summary);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/project-end-date")
    public ResponseEntity<Map<String, Object>> getProjectEndDate() {
        LocalDate endDate = constructionRequestService.getProjectEndDate();
        return ResponseEntity.ok(Map.of("projectEndDate", endDate, "message",
                "Fecha estimada de finalización del proyecto completo"));
    }
}
