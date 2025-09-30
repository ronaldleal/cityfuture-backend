package com.cityfuture.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.cityfuture.domain.model.Coordinate;

public record CreateConstructionOrderRequest(
        @NotBlank(message = "El nombre del proyecto no puede estar vacío")
        @Size(min = 3, max = 100, message = "El nombre del proyecto debe tener entre 3 y 100 caracteres")
        String projectName,
        
        @NotNull(message = "La ubicación es obligatoria")
        @Valid
        Coordinate location,
        
        @NotBlank(message = "El tipo de construcción no puede estar vacío")
        @Size(min = 2, max = 50, message = "El tipo de construcción debe tener entre 2 y 50 caracteres")
        String typeConstruction
) {
    // Método para convertir a ConstructionOrder (sin los campos calculados)
    public com.cityfuture.domain.model.ConstructionOrder toDomain() {
        return new com.cityfuture.domain.model.ConstructionOrder(
            null, // id se genera automáticamente
            projectName,
            location,
            typeConstruction,
            null, // estado se asigna automáticamente como "Pendiente"
            null, // estimatedDays se calcula según el tipo
            null  // entregaDate se calcula automáticamente
        );
    }
}