package com.cityfuture.api.controller;

import com.cityfuture.domain.model.Material;
import com.cityfuture.infrastructure.service.MaterialServiceUseCase;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/materials")
@AllArgsConstructor
public class MaterialController {

    private static final Logger logger = LoggerFactory.getLogger(MaterialController.class);
    private final MaterialServiceUseCase materialServiceUseCase;

    // @PreAuthorize("hasRole('ARQUITECTO')")  // Temporalmente comentado para pruebas
    @PostMapping
    public ResponseEntity<?> createMaterial(@RequestBody Material material) {
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
    public ResponseEntity<Material> getMaterialById(@PathVariable Long id) {
        return ResponseEntity.ok(materialServiceUseCase.getMaterialById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Material> updateMaterial(@PathVariable Long id,
            @RequestBody Material material) {
        return ResponseEntity.ok(materialServiceUseCase.updateMaterial(id, material));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMaterial(@PathVariable Long id) {
        try {
            materialServiceUseCase.deleteMaterial(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(
                    Map.of("error", "No se pudo eliminar el material", "message", e.getMessage()));
        }
    }
}
