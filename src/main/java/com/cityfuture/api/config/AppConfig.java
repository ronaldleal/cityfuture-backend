package com.cityfuture.api.config;

import com.cityfuture.api.controller.ConstructionController;
import com.cityfuture.api.controller.MaterialController;
import com.cityfuture.api.controller.ReportController;
import com.cityfuture.api.controller.TestController;
import com.cityfuture.application.service.MaterialService;
import com.cityfuture.infrastructure.mapper.ConstructionMapper;
import com.cityfuture.infrastructure.mapper.MaterialMapper;
import com.cityfuture.infrastructure.persistence.repository.JpaConstructionOrderRepository;
import com.cityfuture.infrastructure.persistence.repository.JpaMaterialRepository;
import com.cityfuture.infrastructure.persistence.repository.JpaMaterialStockRepository;
import com.cityfuture.infrastructure.service.ConstructionRequestUseCase;
import com.cityfuture.infrastructure.service.MaterialServiceImpl;
import com.cityfuture.infrastructure.service.TestSchedulerService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration("apiAppConfig")
public class AppConfig {

    @Bean
    MaterialController materialController(MaterialService materialService) {
        return new MaterialController(materialService);
    }

    @Bean
    ConstructionRequestUseCase constructionRequestUseCase(JpaConstructionOrderRepository orderRepository,
                                                          ConstructionMapper mapper,
                                                          JpaMaterialRepository materialRepository) {
        return new ConstructionRequestUseCase(orderRepository,mapper,materialRepository);
    }

    @Bean
    MaterialServiceImpl materialService(JpaMaterialRepository materialRepository,
                                        JpaMaterialStockRepository stockRepository,
                                        MaterialMapper mapper) {
        return new MaterialServiceImpl(materialRepository, stockRepository, mapper);
    }

    @Bean
    ReportController reportController(ConstructionRequestUseCase constructionRequestUseCase) {
        return new ReportController(constructionRequestUseCase);
    }


    @Bean
    ConstructionController constructionController(ConstructionRequestUseCase constructionRequestService) {
        return new ConstructionController(constructionRequestService);
    }

    @Bean
    TestController testController(TestSchedulerService testSchedulerService,
                                  ConstructionRequestUseCase constructionRequestService) {
        return new TestController(testSchedulerService, constructionRequestService);
    }
}
