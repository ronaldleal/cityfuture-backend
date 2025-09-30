package com.cityfuture.api.controller;

import com.cityfuture.domain.model.Material;
import com.cityfuture.infrastructure.service.MaterialServiceUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Tag(name = "Materiales", description = "API para gestión de materiales de construcción")
@RestController
@RequestMapping("/api/materials")
@AllArgsConstructor
@Validated
public class MaterialController {

    private static final Logger logger = LoggerFactory.getLogger(MaterialController.class);
    private final MaterialServiceUseCase materialServiceUseCase;

    @Operation(summary = "Crear nuevo material", description = "Crea un nuevo material en el inventario")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Material creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Error de validación"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping
    public ResponseEntity<?> createMaterial(@Valid @RequestBody Material material) {
        logger.info("Solicitud de creación de material - Nombre: {}", material.materialName());

        try {
            Material createdMaterial = materialServiceUseCase.createMaterial(material);
            logger.info("Material creado exitosamente - ID: {}", createdMaterial.id());
            return ResponseEntity.ok(createdMaterial);
        } catch (Exception e) {
            logger.error("Error al crear material - Nombre: {}", material.materialName(), e);
            return ResponseEntity.status(500).body(Map.of("error", "Error interno del servidor",
                    "message", "Error al crear el material", "timestamp", LocalDateTime.now()));
        }
    }

    @GetMapping
    public ResponseEntity<List<Material>> getAllMaterials() {
        return ResponseEntity.ok(materialServiceUseCase.getAllMaterials());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Material> getMaterialById(@PathVariable @NotNull @Min(1) Long id) {
        return ResponseEntity.ok(materialServiceUseCase.getMaterialById(id));
    }

    @PreAuthorize("hasRole('ARQUITECTO')")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateMaterial(@PathVariable @NotNull @Min(1) Long id,
            @Valid @RequestBody Material material) {
        logger.info("Solicitud de actualización de material - ID: {}, Nombre: {}", id, material.materialName());
        
        try {
            Material updatedMaterial = materialServiceUseCase.updateMaterial(id, material);
            logger.info("Material actualizado exitosamente - ID: {}", updatedMaterial.id());
            return ResponseEntity.ok(updatedMaterial);
        } catch (Exception e) {
            logger.error("Error al actualizar material - ID: {}", id, e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "Error interno del servidor",
                "message", "Error al actualizar el material: " + e.getMessage(),
                "timestamp", LocalDateTime.now()
            ));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMaterial(@PathVariable @NotNull @Min(1) Long id) {
        try {
            materialServiceUseCase.deleteMaterial(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(
                    Map.of("error", "No se pudo eliminar el material", "message", e.getMessage()));
        }
    }
}
