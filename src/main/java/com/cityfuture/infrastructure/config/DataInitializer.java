package com.cityfuture.infrastructure.config;

import com.cityfuture.infrastructure.persistence.entity.MaterialEntity;
import com.cityfuture.infrastructure.persistence.repository.JpaMaterialRepository;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@AllArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    
    private final JpaMaterialRepository materialRepository;
    
    @Override
    public void run(String... args) throws Exception {
        initializeMaterials();
    }
    
    private void initializeMaterials() {
        logger.info("Inicializando materiales de construcción...");
        
        List<MaterialData> materialsToCreate = Arrays.asList(
            new MaterialData("Ce", "Cemento", 1000),
            new MaterialData("Gr", "Grava", 800),
            new MaterialData("Ar", "Arena", 1500),
            new MaterialData("Ma", "Madera", 600),
            new MaterialData("Ad", "Adobe", 400)
        );
        
        for (MaterialData materialData : materialsToCreate) {
            if (!materialRepository.findByCode(materialData.code()).isPresent()) {
                MaterialEntity material = new MaterialEntity();
                material.setCode(materialData.code());
                material.setMaterialName(materialData.name());
                material.setQuantity(materialData.quantity());
                
                materialRepository.save(material);
                logger.info("Material creado: {} - {} (Cantidad: {})", 
                    material.getCode(), material.getMaterialName(), material.getQuantity());
            } else {
                logger.debug("Material ya existe: {}", materialData.code());
            }
        }
        
        logger.info("Inicialización de materiales completada.");
    }
    
    private record MaterialData(String code, String name, int quantity) {}
}