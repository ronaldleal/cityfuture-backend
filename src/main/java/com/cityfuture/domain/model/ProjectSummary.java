package com.cityfuture.domain.model;

import java.time.LocalDate;

public record ProjectSummary(Integer totalConstructionDays, LocalDate projectStartDate,
        LocalDate projectEndDate, LocalDate estimatedDeliveryDate, Integer totalOrders,
        String status) {
}
