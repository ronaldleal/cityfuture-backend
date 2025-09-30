package com.cityfuture.domain.model;

import jakarta.validation.constraints.*;

public record Material(
        Long id,
        
        @NotBlank(message = "El nombre del material no puede estar vacío")
        @Size(min = 2, max = 100, message = "El nombre del material debe tener entre 2 y 100 caracteres")
        String materialName,
        
        @NotBlank(message = "El código del material no puede estar vacío")
        @Size(min = 2, max = 20, message = "El código del material debe tener entre 2 y 20 caracteres")
        @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "El código debe contener solo letras, números, guiones y guiones bajos")
        String code,
        
        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 0, message = "La cantidad no puede ser negativa")
        @Max(value = 999999, message = "La cantidad no puede exceder 999,999 unidades")
        Integer quantity
) {}