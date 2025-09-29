package com.cityfuture.infrastructure.service;

import com.cityfuture.application.service.ReportService;
import com.cityfuture.application.service.SchedulerService;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class SchedulerServiceImpl implements SchedulerService {

    private final ReportService reportService;

    @Override
    @Scheduled(cron = "0 0 8 * * *") // Ejecuta todos los días a las 8 AM
    public void scheduleDailyReports() {
        String report = reportService.generateMaterialReport();
        System.out.println("📊 Reporte automático: " + report);
    }
}