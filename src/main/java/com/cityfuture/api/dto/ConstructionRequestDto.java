package com.cityfuture.api.dto;

import lombok.Data;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

@Data
public class ConstructionRequestDto {
    
    @NotBlank(message = "El tipo de construcción no puede estar vacío")
    @Size(min = 2, max = 50, message = "El tipo de construcción debe tener entre 2 y 50 caracteres")
    private String constructionType;
    
    @NotNull(message = "La coordenada X es obligatoria")
    @Min(value = 0, message = "La coordenada X debe ser mayor o igual a 0")
    @Max(value = 1000, message = "La coordenada X debe ser menor o igual a 1000")
    private int x;
    
    @NotNull(message = "La coordenada Y es obligatoria")
    @Min(value = 0, message = "La coordenada Y debe ser mayor o igual a 0")
    @Max(value = 1000, message = "La coordenada Y debe ser menor o igual a 1000")
    private int y;
    
    @NotNull(message = "La fecha de solicitud es obligatoria")
    @PastOrPresent(message = "La fecha de solicitud no puede ser futura")
    private LocalDate requestDate;
}
