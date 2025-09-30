package com.cityfuture.api.controller;

import com.cityfuture.domain.model.Material;
import com.cityfuture.infrastructure.service.MaterialServiceUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(MaterialController.class)
class MaterialControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MaterialServiceUseCase materialServiceUseCase;

    @Autowired
    private ObjectMapper objectMapper;

    private Material testMaterial;
    private List<Material> testMaterials;

    @BeforeEach
    void setUp() {
        testMaterial = new Material(1L, "Cemento", "Ce", 100);
        testMaterials = Arrays.asList(
            testMaterial,
            new Material(2L, "Arena", "Ar", 200),
            new Material(3L, "Grava", "Gr", 150)
        );
    }

    @Test
    @WithMockUser(roles = "ARQUITECTO")
    void createMaterial_ValidMaterial_ReturnsCreatedMaterial() throws Exception {
        // Arrange
        Material materialToCreate = new Material(null, "Cemento", "Ce", 100);
        when(materialServiceUseCase.createMaterial(any(Material.class))).thenReturn(testMaterial);

        // Act & Assert
        mockMvc.perform(post("/api/materials")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(materialToCreate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.materialName").value("Cemento"))
                .andExpect(jsonPath("$.code").value("Ce"))
                .andExpect(jsonPath("$.quantity").value(100));

        verify(materialServiceUseCase).createMaterial(any(Material.class));
    }

    @Test
    @WithMockUser(roles = "ARQUITECTO")
    void createMaterial_InvalidMaterial_ReturnsBadRequest() throws Exception {
        // Arrange
        Material invalidMaterial = new Material(null, "", "", -1);

        // Act & Assert
        mockMvc.perform(post("/api/materials")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidMaterial)))
                .andExpect(status().isBadRequest());

        verify(materialServiceUseCase, never()).createMaterial(any(Material.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void createMaterial_NonArquitectoRole_ReturnsForbidden() throws Exception {
        // Arrange
        Material materialToCreate = new Material(null, "Cemento", "Ce", 100);

        // Act & Assert
        mockMvc.perform(post("/api/materials")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(materialToCreate)))
                .andExpect(status().isForbidden());

        verify(materialServiceUseCase, never()).createMaterial(any(Material.class));
    }

    @Test
    void getAllMaterials_ReturnsAllMaterials() throws Exception {
        // Arrange
        when(materialServiceUseCase.getAllMaterials()).thenReturn(testMaterials);

        // Act & Assert
        mockMvc.perform(get("/api/materials"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].materialName").value("Cemento"))
                .andExpect(jsonPath("$[1].materialName").value("Arena"))
                .andExpect(jsonPath("$[2].materialName").value("Grava"));

        verify(materialServiceUseCase).getAllMaterials();
    }

    @Test
    void getMaterialById_ExistingId_ReturnsMaterial() throws Exception {
        // Arrange
        when(materialServiceUseCase.getMaterialById(1L)).thenReturn(testMaterial);

        // Act & Assert
        mockMvc.perform(get("/api/materials/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.materialName").value("Cemento"))
                .andExpect(jsonPath("$.code").value("Ce"))
                .andExpect(jsonPath("$.quantity").value(100));

        verify(materialServiceUseCase).getMaterialById(1L);
    }

    @Test
    void getMaterialById_InvalidId_ReturnsBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/materials/0"))
                .andExpect(status().isBadRequest());

        verify(materialServiceUseCase, never()).getMaterialById(anyLong());
    }

    @Test
    @WithMockUser(roles = "ARQUITECTO")
    void updateMaterial_ValidUpdate_ReturnsUpdatedMaterial() throws Exception {
        // Arrange
        Material updatedMaterial = new Material(1L, "Cemento Mejorado", "Ce", 150);
        when(materialServiceUseCase.updateMaterial(eq(1L), any(Material.class))).thenReturn(updatedMaterial);

        // Act & Assert
        mockMvc.perform(put("/api/materials/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedMaterial)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.materialName").value("Cemento Mejorado"))
                .andExpect(jsonPath("$.quantity").value(150));

        verify(materialServiceUseCase).updateMaterial(eq(1L), any(Material.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateMaterial_NonArquitectoRole_ReturnsForbidden() throws Exception {
        // Arrange
        Material updatedMaterial = new Material(1L, "Cemento Mejorado", "Ce", 150);

        // Act & Assert
        mockMvc.perform(put("/api/materials/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedMaterial)))
                .andExpect(status().isForbidden());

        verify(materialServiceUseCase, never()).updateMaterial(anyLong(), any(Material.class));
    }

    @Test
    @WithMockUser(roles = "ARQUITECTO")
    void updateMaterial_ServiceThrowsException_ReturnsInternalServerError() throws Exception {
        // Arrange
        Material updatedMaterial = new Material(1L, "Cemento Mejorado", "Ce", 150);
        when(materialServiceUseCase.updateMaterial(eq(1L), any(Material.class)))
            .thenThrow(new RuntimeException("Material not found"));

        // Act & Assert
        mockMvc.perform(put("/api/materials/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedMaterial)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Error interno del servidor"));

        verify(materialServiceUseCase).updateMaterial(eq(1L), any(Material.class));
    }

    @Test
    @WithMockUser(roles = "ARQUITECTO")
    void deleteMaterial_ExistingId_ReturnsNoContent() throws Exception {
        // Arrange
        doNothing().when(materialServiceUseCase).deleteMaterial(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/materials/1")
                .with(csrf()))
                .andExpect(status().isNoContent());

        verify(materialServiceUseCase).deleteMaterial(1L);
    }

    @Test
    @WithMockUser(roles = "ARQUITECTO")
    void deleteMaterial_ServiceThrowsException_ReturnsNotFound() throws Exception {
        // Arrange
        doThrow(new RuntimeException("Material not found")).when(materialServiceUseCase).deleteMaterial(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/materials/1")
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("No se pudo eliminar el material"));

        verify(materialServiceUseCase).deleteMaterial(1L);
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteMaterial_NonArquitectoRole_ReturnsForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/materials/1")
                .with(csrf()))
                .andExpect(status().isForbidden());

        verify(materialServiceUseCase, never()).deleteMaterial(anyLong());
    }
}