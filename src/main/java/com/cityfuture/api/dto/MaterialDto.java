package com.cityfuture.api.dto;

import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class MaterialDto {
    
    @NotBlank(message = "El tipo de material no puede estar vacío")
    @Size(min = 2, max = 100, message = "El tipo de material debe tener entre 2 y 100 caracteres")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$", message = "El tipo de material solo puede contener letras y espacios")
    private String materialType;
    
    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser mayor a 0")
    @Max(value = 999999, message = "La cantidad no puede exceder 999,999 unidades")
    private int quantity;
}
