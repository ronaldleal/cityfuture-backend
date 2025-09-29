package com.cityfuture.api.config;

import com.cityfuture.api.controller.ConstructionController;
import com.cityfuture.infrastructure.mapper.ConstructionMapper;
import com.cityfuture.infrastructure.mapper.MaterialMapper;
import com.cityfuture.infrastructure.persistence.repository.JpaConstructionOrderRepository;
import com.cityfuture.infrastructure.persistence.repository.JpaMaterialRepository;
import com.cityfuture.infrastructure.persistence.repository.JpaMaterialStockRepository;
import com.cityfuture.infrastructure.service.ConstructionRequestUseCase;
import com.cityfuture.infrastructure.service.MaterialServiceUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {


    @Bean
    ConstructionRequestUseCase constructionRequestUseCase(JpaConstructionOrderRepository orderRepository,
                                                          ConstructionMapper mapper,
                                                          JpaMaterialRepository materialRepository) {
        return new ConstructionRequestUseCase(orderRepository, mapper, materialRepository);
    }

    @Bean
    MaterialServiceUseCase materialServiceUseCase(JpaMaterialRepository materialRepository,
                                                  JpaMaterialStockRepository stockRepository,
                                                  MaterialMapper mapper) {
        return new MaterialServiceUseCase(materialRepository, stockRepository, mapper);
    }

    @Bean
    ConstructionController constructionController(ConstructionRequestUseCase constructionRequestUseCase) {
        return new ConstructionController(constructionRequestUseCase);
    }
}
