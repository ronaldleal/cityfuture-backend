package com.cityfuture.application.service;

import com.cityfuture.domain.model.Material;

import java.util.List;

public interface MaterialService {
    Material createMaterial(Material material);

    List<Material> getAllMaterials();

    Material getMaterialById(Long id);

    Material updateMaterial(Long id, Material material);

    void deleteMaterial(Long id);
}
