package com.cityfuture.domain.model;

public record Material(
        Long id,
        String materialName,
        String code,
        Integer quantity
) {}