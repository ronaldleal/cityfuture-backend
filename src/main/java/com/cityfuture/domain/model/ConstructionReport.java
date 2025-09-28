package com.cityfuture.domain.model;

import java.time.LocalDate;
import java.util.Map;

public record ConstructionReport(LocalDate reportDate, int totalOrders, int pendingOrders,
        int inProgressOrders, int finishedOrders, Map<String, Integer> pendingByType,
        Map<String, Integer> inProgressByType, Map<String, Integer> finishedByType,
        ProjectSummary projectSummary) {
}
