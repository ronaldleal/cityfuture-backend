package com.cityfuture.api.controller;

import com.cityfuture.api.dto.CreateConstructionOrderRequest;
import com.cityfuture.domain.model.ConstructionOrder;
import com.cityfuture.domain.model.Coordinate;
import com.cityfuture.infrastructure.service.ConstructionRequestUseCase;
import com.cityfuture.infrastructure.service.ReportServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(ConstructionController.class)
class ConstructionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ConstructionRequestUseCase constructionRequestService;

    @MockitoBean
    private ReportServiceImpl reportService;

    @Autowired
    private ObjectMapper objectMapper;

    private ConstructionOrder testOrder;
    private CreateConstructionOrderRequest testRequest;
    private List<ConstructionOrder> testOrders;

    @BeforeEach
    void setUp() {
        Coordinate coordinate = new Coordinate(10.123, -74.567);
        testOrder = new ConstructionOrder(
            1L,
            "Casa del Futuro",
            coordinate,
            "CASA",
            "Pendiente",
            3,
            LocalDate.now().plusDays(3)
        );

        testRequest = new CreateConstructionOrderRequest(
            "Casa del Futuro",
            coordinate,
            "CASA"
        );

        testOrders = Arrays.asList(
            testOrder,
            new ConstructionOrder(2L, "Edificio Central", coordinate, "EDIFICIO", "En Progreso", 
                6, LocalDate.now().plusDays(6))
        );
    }

    @Test
    @WithMockUser(roles = "ARQUITECTO")
    void createOrder_ValidRequest_ReturnsCreatedOrder() throws Exception {
        // Arrange
        when(constructionRequestService.createOrder(any(ConstructionOrder.class))).thenReturn(testOrder);

        // Act & Assert
        mockMvc.perform(post("/api/constructions")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.projectName").value("Casa del Futuro"))
                .andExpect(jsonPath("$.typeConstruction").value("CASA"))
                .andExpect(jsonPath("$.estado").value("Pendiente"));

        verify(constructionRequestService).createOrder(any(ConstructionOrder.class));
    }

    @Test
    @WithMockUser(roles = "ARQUITECTO")
    void createOrder_InvalidRequest_ReturnsBadRequest() throws Exception {
        // Arrange
        CreateConstructionOrderRequest invalidRequest = new CreateConstructionOrderRequest("", null, "");

        // Act & Assert
        mockMvc.perform(post("/api/constructions")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(constructionRequestService, never()).createOrder(any(ConstructionOrder.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void createOrder_NonArquitectoRole_ReturnsForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/constructions")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isForbidden());

        verify(constructionRequestService, never()).createOrder(any(ConstructionOrder.class));
    }

    @Test
    void getAllOrders_NoStatusFilter_ReturnsAllOrders() throws Exception {
        // Arrange
        when(constructionRequestService.getAllOrders()).thenReturn(testOrders);

        // Act & Assert
        mockMvc.perform(get("/api/constructions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].projectName").value("Casa del Futuro"))
                .andExpect(jsonPath("$[1].projectName").value("Edificio Central"));

        verify(constructionRequestService).getAllOrders();
    }

    @Test
    void getAllOrders_WithStatusFilter_ReturnsAllOrders() throws Exception {
        // Arrange
        when(constructionRequestService.getAllOrders()).thenReturn(testOrders);

        // Act & Assert
        mockMvc.perform(get("/api/constructions")
                .param("estado", "todos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(constructionRequestService).getAllOrders();
    }

    @Test
    void getOrderById_ExistingOrder_ReturnsOrder() throws Exception {
        // Arrange
        when(constructionRequestService.getOrderById(1L)).thenReturn(testOrder);

        // Act & Assert
        mockMvc.perform(get("/api/constructions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.projectName").value("Casa del Futuro"));

        verify(constructionRequestService).getOrderById(1L);
    }

    @Test
    @WithMockUser(roles = "ARQUITECTO")
    void updateOrder_ValidUpdate_ReturnsUpdatedOrder() throws Exception {
        // Arrange
        ConstructionOrder updatedOrder = new ConstructionOrder(
            1L, "Casa Moderna", testOrder.location(), "CASA", "En Progreso",
            testOrder.estimatedDays(), testOrder.entregaDate()
        );
        when(constructionRequestService.updateOrder(eq(1L), any(ConstructionOrder.class)))
            .thenReturn(updatedOrder);

        // Act & Assert
        mockMvc.perform(put("/api/constructions/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedOrder)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectName").value("Casa Moderna"))
                .andExpect(jsonPath("$.estado").value("En Progreso"));

        verify(constructionRequestService).updateOrder(eq(1L), any(ConstructionOrder.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateOrder_NonArquitectoRole_ReturnsForbidden() throws Exception {
        // Arrange
        ConstructionOrder updatedOrder = new ConstructionOrder(
            1L, "Casa Moderna", testOrder.location(), "CASA", "En Progreso",
            testOrder.estimatedDays(), testOrder.entregaDate()
        );

        // Act & Assert
        mockMvc.perform(put("/api/constructions/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedOrder)))
                .andExpect(status().isForbidden());

        verify(constructionRequestService, never()).updateOrder(anyLong(), any(ConstructionOrder.class));
    }

    @Test
    @WithMockUser(roles = "ARQUITECTO")
    void deleteOrder_ExistingOrder_ReturnsNoContent() throws Exception {
        // Arrange
        doNothing().when(constructionRequestService).deleteOrder(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/constructions/1")
                .with(csrf()))
                .andExpect(status().isNoContent());

        verify(constructionRequestService).deleteOrder(1L);
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteOrder_NonArquitectoRole_ReturnsForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/constructions/1")
                .with(csrf()))
                .andExpect(status().isForbidden());

        verify(constructionRequestService, never()).deleteOrder(anyLong());
    }

    @Test
    @WithMockUser(roles = "ARQUITECTO")
    void validateOrder_ValidOrder_ReturnsValidationResult() throws Exception {
        // Arrange
        Map<String, Object> validationResult = Map.of(
            "valid", true,
            "message", "La solicitud de construcción puede realizarse",
            "estimatedDays", 3
        );
        when(constructionRequestService.validateConstructionRequest(any(ConstructionOrder.class)))
            .thenReturn(validationResult);

        // Act & Assert
        mockMvc.perform(post("/api/constructions/validate")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.estimatedDays").value(3));

        verify(constructionRequestService).validateConstructionRequest(any(ConstructionOrder.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void validateOrder_NonArquitectoRole_ReturnsForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/constructions/validate")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isForbidden());

        verify(constructionRequestService, never()).validateConstructionRequest(any(ConstructionOrder.class));
    }
}