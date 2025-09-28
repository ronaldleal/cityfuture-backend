package com.cityfuture.api.dto;

import java.time.LocalDate;
import lombok.Data;

@Data
public class ConstructionRequestDto {
    private String constructionType; // Ej: "Casa", "Lago", etc.
    private int x;
    private int y;
    private LocalDate requestDate;
}
