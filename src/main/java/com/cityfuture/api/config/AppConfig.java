package com.cityfuture.api.config;

import com.cityfuture.api.controller.MaterialController;
import com.cityfuture.api.controller.ReportController;
import com.cityfuture.application.service.ConstructionRequestService;
import com.cityfuture.application.service.MaterialService;
import com.cityfuture.infrastructure.mapper.MaterialMapper;
import com.cityfuture.infrastructure.persistence.repository.JpaMaterialRepository;
import com.cityfuture.infrastructure.persistence.repository.JpaMaterialStockRepository;
import com.cityfuture.infrastructure.service.MaterialServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    MaterialController materialController(MaterialService materialService) {
        return new MaterialController(materialService);
    }

    @Bean
    ReportController reportController(ConstructionRequestService constructionRequestService) {
        return new ReportController(constructionRequestService);
    }

    @Bean
    MaterialServiceImpl materialService(JpaMaterialRepository materialRepository,
                                        JpaMaterialStockRepository stockRepository,
                                        MaterialMapper mapper) {
        return new MaterialServiceImpl(materialRepository, stockRepository,mapper);
    }


}
