package com.cityfuture.api.controller;

import com.cityfuture.domain.model.ConstructionReport;
import com.cityfuture.domain.model.ProjectSummary;
import com.cityfuture.infrastructure.service.ConstructionRequestUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(ReportController.class)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ConstructionRequestUseCase constructionRequestService;

    private ConstructionReport sampleReport;
    private ProjectSummary sampleProjectSummary;

    @BeforeEach
    void setUp() {
        // Preparar datos de prueba
        sampleProjectSummary = new ProjectSummary(
            25,
            LocalDate.of(2024, 12, 1),
            LocalDate.of(2024, 12, 30),
            LocalDate.of(2024, 12, 30),
            5,
            "En progreso"
        );

        sampleReport = new ConstructionReport(
            LocalDate.of(2024, 12, 18),
            5, 2, 1, 2,
            Map.of("Casa", 1, "Edificio", 1),
            Map.of("Lago", 1),
            Map.of("Casa", 1, "Piscina", 1),
            sampleProjectSummary
        );
    }

    @Test
    @WithMockUser
    void getConstructionReport_Success_ReturnsReport() throws Exception {
        // Arrange
        when(constructionRequestService.generateConstructionReport()).thenReturn(sampleReport);

        // Act & Assert
        mockMvc.perform(get("/api/reports/constructions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reportDate").value("2024-12-18"))
                .andExpect(jsonPath("$.totalOrders").value(5))
                .andExpect(jsonPath("$.pendingOrders").value(2))
                .andExpect(jsonPath("$.inProgressOrders").value(1))
                .andExpect(jsonPath("$.finishedOrders").value(2))
                .andExpect(jsonPath("$.pendingByType.Casa").value(1))
                .andExpect(jsonPath("$.pendingByType.Edificio").value(1))
                .andExpect(jsonPath("$.inProgressByType.Lago").value(1))
                .andExpect(jsonPath("$.finishedByType.Casa").value(1))
                .andExpect(jsonPath("$.finishedByType.Piscina").value(1))
                .andExpect(jsonPath("$.projectSummary.totalOrders").value(5))
                .andExpect(jsonPath("$.projectSummary.status").value("En progreso"));

        verify(constructionRequestService).generateConstructionReport();
    }

    @Test
    @WithMockUser
    void getConstructionReport_ServiceThrowsException_ReturnsInternalServerError() throws Exception {
        // Arrange
        when(constructionRequestService.generateConstructionReport())
            .thenThrow(new RuntimeException("Error generando reporte"));

        // Act & Assert
        mockMvc.perform(get("/api/reports/constructions"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Error interno del servidor"))
                .andExpect(jsonPath("$.message").value("Error al generar el reporte de construcciones"));

        verify(constructionRequestService).generateConstructionReport();
    }

    @Test
    void getConstructionReport_Unauthenticated_ReturnsUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/reports/constructions"))
                .andExpect(status().isUnauthorized());

        verify(constructionRequestService, never()).generateConstructionReport();
    }

    @Test
    @WithMockUser
    void getProjectSummary_Success_ReturnsSummary() throws Exception {
        // Arrange
        when(constructionRequestService.getProjectSummary()).thenReturn(sampleProjectSummary);

        // Act & Assert
        mockMvc.perform(get("/api/reports/project-summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalConstructionDays").value(25))
                .andExpect(jsonPath("$.projectStartDate").value("2024-12-01"))
                .andExpect(jsonPath("$.projectEndDate").value("2024-12-30"))
                .andExpect(jsonPath("$.estimatedDeliveryDate").value("2024-12-30"))
                .andExpect(jsonPath("$.totalOrders").value(5))
                .andExpect(jsonPath("$.status").value("En progreso"));

        verify(constructionRequestService).getProjectSummary();
    }

    @Test
    @WithMockUser
    void getProjectSummary_ServiceThrowsException_ReturnsInternalServerError() throws Exception {
        // Arrange
        when(constructionRequestService.getProjectSummary())
            .thenThrow(new RuntimeException("Error generando resumen"));

        // Act & Assert
        mockMvc.perform(get("/api/reports/project-summary"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Error interno del servidor"))
                .andExpect(jsonPath("$.message").value("Error al generar el resumen del proyecto"));

        verify(constructionRequestService).getProjectSummary();
    }

    @Test
    void getProjectSummary_Unauthenticated_ReturnsUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/reports/project-summary"))
                .andExpect(status().isUnauthorized());

        verify(constructionRequestService, never()).getProjectSummary();
    }

    @Test
    @WithMockUser
    void getProjectEndDate_Success_ReturnsEndDate() throws Exception {
        // Arrange
        LocalDate endDate = LocalDate.of(2024, 12, 30);
        when(constructionRequestService.getProjectEndDate()).thenReturn(endDate);

        // Act & Assert
        mockMvc.perform(get("/api/reports/project-end-date"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectEndDate").value("2024-12-30"))
                .andExpect(jsonPath("$.message").value("Fecha estimada de finalización del proyecto completo"));

        verify(constructionRequestService).getProjectEndDate();
    }

    @Test
    @WithMockUser
    void getProjectEndDate_ServiceThrowsException_ReturnsInternalServerError() throws Exception {
        // Arrange
        when(constructionRequestService.getProjectEndDate())
            .thenThrow(new RuntimeException("Error calculando fecha"));

        // Act & Assert
        mockMvc.perform(get("/api/reports/project-end-date"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Error interno del servidor"))
                .andExpect(jsonPath("$.message").value("Error al calcular la fecha de finalización"));

        verify(constructionRequestService).getProjectEndDate();
    }

    @Test
    void getProjectEndDate_Unauthenticated_ReturnsUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/reports/project-end-date"))
                .andExpect(status().isUnauthorized());

        verify(constructionRequestService, never()).getProjectEndDate();
    }
}