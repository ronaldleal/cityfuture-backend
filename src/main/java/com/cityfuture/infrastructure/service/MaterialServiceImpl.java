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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;

@Primary
@Service
public class MaterialServiceImpl implements MaterialService {

    private static final Logger logger = LoggerFactory.getLogger(MaterialServiceImpl.class);

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
        logger.info("Iniciando creación de material - Nombre: {}", material.materialName());

        try {
            if (materialExists(material.materialName())) {
                logger.warn("Intento de crear material existente - Nombre: {}",
                        material.materialName());
                throw new MaterialAlreadyExistsException(
                        "El material con nombre '" + material.materialName() + "' ya existe");
            }

            MaterialEntity entity = mapper.toEntity(material);
            MaterialEntity saved = materialRepository.save(entity);
            logger.debug("Material guardado en BD - ID: {}", saved.getId());

            // Crear stock inicial si es necesario
            if (stockRepository.findByMaterialId(saved.getId()).isEmpty()) {
                MaterialStockEntity stock = new MaterialStockEntity();
                stock.setMaterial(saved);
                stock.setQuantity(0);
                stockRepository.save(stock);
                logger.debug("Stock inicial creado para material ID: {}", saved.getId());
            }

            logger.info("Material creado exitosamente - ID: {}, Nombre: {}", saved.getId(),
                    saved.getMaterialName());
            return mapper.toDomain(saved);

        } catch (MaterialAlreadyExistsException e) {
            logger.error("Error al crear material - Ya existe: {}", material.materialName(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Error inesperado al crear material - Nombre: {}", material.materialName(),
                    e);
            throw new RuntimeException("Error interno al crear el material", e);
        }
    }

    @Override
    public List<Material> getAllMaterials() {
        return materialRepository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public Material getMaterialById(Long id) {
        return materialRepository.findById(id).map(mapper::toDomain).orElseThrow(
                () -> new MaterialNotFoundException("No existe un material con el ID: " + id));
    }

    @Override
    public Material updateMaterial(Long id, Material material) {
        validateMaterialNameNotExistsForUpdate(material.materialName(), id);

        return materialRepository.findById(id).map(existing -> {
            MaterialEntity updated = mapper.toEntity(material);
            updated.setId(existing.getId());
            return mapper.toDomain(materialRepository.save(updated));
        }).orElseThrow(
                () -> new MaterialNotFoundException("No existe un material con el ID: " + id));
    }

    private void validateMaterialNameNotExistsForUpdate(String materialName, Long excludeId) {
        materialRepository.findByMaterialName(materialName)
                .filter(existing -> !existing.getId().equals(excludeId)).ifPresent(existing -> {
                    throw new MaterialAlreadyExistsException("El material con nombre '"
                            + materialName + "' ya existe con ID: " + existing.getId());
                });
    }

    private boolean materialExists(String materialName) {
        return materialRepository.findByMaterialName(materialName).isPresent();
    }

    @Override
    public void deleteMaterial(Long id) {
        logger.info("Iniciando eliminación de material - ID: {}", id);

        try {
            if (!materialRepository.existsById(id)) {
                logger.warn("Intento de eliminar material inexistente - ID: {}", id);
                throw new RuntimeException("No existe un material con el ID: " + id);
            }

            materialRepository.deleteById(id);
            logger.info("Material eliminado exitosamente - ID: {}", id);

        } catch (RuntimeException e) {
            logger.error("Error al eliminar material - ID: {}", id, e);
            throw e;
        } catch (Exception e) {
            logger.error("Error inesperado al eliminar material - ID: {}", id, e);
            throw new RuntimeException("Error interno al eliminar el material", e);
        }
    }
}
