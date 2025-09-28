package com.cityfuture.domain.model;

public record MaterialStock(
        Integer availableQuantity,
        String warehouseLocation
) {}