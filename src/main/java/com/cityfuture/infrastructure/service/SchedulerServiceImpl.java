package com.cityfuture.infrastructure.service;

import com.cityfuture.application.service.ReportService;
import com.cityfuture.application.service.SchedulerService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class SchedulerServiceImpl implements SchedulerService {

    private final ReportService reportService;

    public SchedulerServiceImpl(ReportService reportService) {
        this.reportService = reportService;
    }

    @Override
    @Scheduled(cron = "0 0 8 * * *") // Ejecuta todos los días a las 8 AM
    public void scheduleDailyReports() {
        String report = reportService.generateMaterialReport();
        System.out.println("📊 Reporte automático: " + report);
    }
}