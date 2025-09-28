package com.cityfuture.api.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ConstructionRequestDto {
    private String constructionType; // Ej: "Casa", "Lago", etc.
    private int x;
    private int y;
    private LocalDate requestDate;
}
