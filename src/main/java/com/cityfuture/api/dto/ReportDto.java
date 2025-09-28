package com.cityfuture.api.dto;

import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class ReportDto {
    private List<String> pendingConstructions; // IDs o nombres de construcciones pendientes
    private Map<String, Integer> finishedByType; // Ej: {"Casa": 2, "Lago": 1}
    private Map<String, Integer> inProgressByType; // Ej: {"Edificio": 1}
    private String projectEndDate;
}
