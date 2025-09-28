package com.cityfuture.infrastructure.service;

import com.cityfuture.api.exception.MaterialAlreadyExistsException;
import com.cityfuture.api.exception.MaterialNotFoundException;
import com.cityfuture.application.service.MaterialService;
import com.cityfuture.domain.model.Material;
import com.cityfuture.infrastructure.mapper.MaterialMapper;
import com.cityfuture.infrastructure.persistence.entity.MaterialEntity;
import com.cityfuture.infrastructure.persistence.entity.MaterialStockEntity;
import com.cityfuture.infrastructure.persistence.repository.JpaMaterialRepository;
import com.cityfuture.infrastructure.persistence.repository.JpaMaterialStockRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;

@Primary
@Service
public class MaterialServiceImpl implements MaterialService {
    private final JpaMaterialRepository materialRepository;
    private final JpaMaterialStockRepository stockRepository;
    private final MaterialMapper mapper;

    public MaterialServiceImpl(JpaMaterialRepository materialRepository,
                               JpaMaterialStockRepository stockRepository, MaterialMapper mapper) {
        this.materialRepository = materialRepository;
        this.stockRepository = stockRepository;
        this.mapper = mapper;
    }

    @Override
    public Material createMaterial(Material material) {
        if (materialExists(material.materialName())) {
            throw new MaterialAlreadyExistsException("El material con nombre '" + material.materialName() + "' ya existe");
        }

        MaterialEntity entity = mapper.toEntity(material);
        MaterialEntity saved = materialRepository.save(entity);

        // Crear stock inicial si es necesario
        if (stockRepository.findByMaterialId(saved.getId()).isEmpty()) {
            MaterialStockEntity stock = new MaterialStockEntity();
            stock.setMaterial(saved);
            stock.setQuantity(0);
            stockRepository.save(stock);
        }

        return mapper.toDomain(saved);
    }

    @Override
    public List<Material> getAllMaterials() {
        return materialRepository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public Material getMaterialById(Long id) {
        return materialRepository.findById(id)
                .map(mapper::toDomain)
                .orElseThrow(() -> new MaterialNotFoundException("No existe un material con el ID: " + id));
    }

    @Override
    public Material updateMaterial(Long id, Material material) {
        validateMaterialNameNotExistsForUpdate(material.materialName(), id);

        return materialRepository.findById(id)
                .map(existing -> {
                    MaterialEntity updated = mapper.toEntity(material);
                    updated.setId(existing.getId());
                    return mapper.toDomain(materialRepository.save(updated));
                })
                .orElseThrow(() -> new MaterialNotFoundException("No existe un material con el ID: " + id));
    }

    private void validateMaterialNameNotExistsForUpdate(String materialName, Long excludeId) {
        materialRepository.findByMaterialName(materialName)
                .filter(existing -> !existing.getId().equals(excludeId))
                .ifPresent(existing -> {
                    throw new MaterialAlreadyExistsException("El material con nombre '" + materialName + "' ya existe con ID: " + existing.getId());
                });
    }

    private boolean materialExists(String materialName) {
        return materialRepository.findByMaterialName(materialName).isPresent();
    }

    @Override
    public void deleteMaterial(Long id) {
        if (!materialRepository.existsById(id)) {
            throw new RuntimeException("No existe un material con el ID: " + id);
        }
        stockRepository.deleteById(id);
        materialRepository.deleteById(id);
    }

//    @Override
//    public MaterialStock getStock(Long materialId) {
//        return stockRepository.findByMaterialId(materialId).map(mapper::toDomain).orElseThrow(
//                () -> new RuntimeException("Stock not found for material " + materialId));
//    }
//
//    @Override
//    public MaterialStock updateStock(Long materialId, int quantity) {
//        return stockRepository.findByMaterialId(materialId).map(existing -> {
//            existing.setQuantity(existing.getQuantity() + quantity);
//            return mapper.toDomain(stockRepository.save(existing));
//        }).orElseThrow(() -> new RuntimeException("Stock not found for material " + materialId));
//    }
}
