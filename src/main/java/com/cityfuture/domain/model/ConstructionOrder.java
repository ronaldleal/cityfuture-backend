package com.cityfuture.domain.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record ConstructionOrder(
        Long id,
        
        @NotBlank(message = "El nombre del proyecto no puede estar vacío")
        @Size(min = 3, max = 100, message = "El nombre del proyecto debe tener entre 3 y 100 caracteres")
        String projectName,
        
        @NotNull(message = "La ubicación es obligatoria")
        @Valid
        Coordinate location,
        
        @NotBlank(message = "El tipo de construcción no puede estar vacío")
        @Size(min = 2, max = 50, message = "El tipo de construcción debe tener entre 2 y 50 caracteres")
        String typeConstruction,
        
        @NotBlank(message = "El estado no puede estar vacío")
        @Pattern(regexp = "^(Pendiente|En Progreso|Finalizado)$", message = "El estado debe ser: Pendiente, En Progreso o Finalizado")
        String estado,
        
        @NotNull(message = "Los días estimados son obligatorios")
        @Min(value = 1, message = "Los días estimados deben ser mayor a 0")
        @Max(value = 365, message = "Los días estimados no pueden exceder 365 días")
        Integer estimatedDays,
        
        @NotNull(message = "La fecha de entrega es obligatoria")
        @Future(message = "La fecha de entrega debe ser futura")
        LocalDate entregaDate
) {
}
