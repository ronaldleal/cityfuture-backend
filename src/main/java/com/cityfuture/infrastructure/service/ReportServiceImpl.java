package com.cityfuture.infrastructure.service;

import com.cityfuture.application.service.ReportService;
import com.cityfuture.domain.model.ConstructionOrder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReportServiceImpl implements ReportService {

    @Override
    public String generateMaterialReport() {
        return "Reporte de Materiales generado correctamente.";
    }

    @Override
    public String generateConstructionReport(List<ConstructionOrder> orders) {
        return "Reporte de Construcciones: total órdenes = " + orders.size();
    }
}
