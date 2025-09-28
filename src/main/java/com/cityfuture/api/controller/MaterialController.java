package com.cityfuture.api.controller;

import com.cityfuture.application.service.MaterialService;
import com.cityfuture.domain.model.Material;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/materials")
public class MaterialController {

    private final MaterialService materialService;

    public MaterialController(MaterialService materialService) {
        this.materialService = materialService;
    }

    @PostMapping
    public ResponseEntity<Material> createMaterial(@RequestBody Material material) {
        return ResponseEntity.ok(materialService.createMaterial(material));
    }

    @GetMapping
    public ResponseEntity<List<Material>> getAllMaterials() {
        return ResponseEntity.ok(materialService.getAllMaterials());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Material> getMaterialById(@PathVariable Long id) {
        return ResponseEntity.ok(materialService.getMaterialById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Material> updateMaterial(
            @PathVariable Long id,
            @RequestBody Material material) {
        return ResponseEntity.ok(materialService.updateMaterial(id, material));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMaterial(@PathVariable Long id) {
        try {
            materialService.deleteMaterial(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of(
                    "error", "No se pudo eliminar el material",
                    "message", e.getMessage()
            ));
        }
    }
}
