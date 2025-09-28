package com.cityfuture.application.service;

import com.cityfuture.domain.model.ConstructionOrder;

import java.util.List;

public interface ReportService {
    String generateMaterialReport();
    String generateConstructionReport(List<ConstructionOrder> orders);
}
