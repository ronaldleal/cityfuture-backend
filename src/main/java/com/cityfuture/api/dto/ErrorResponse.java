package com.cityfuture.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import jakarta.validation.constraints.*;

@Data
@AllArgsConstructor
public class ErrorResponse {
    
    @NotBlank(message = "El código de error no puede estar vacío")
    @Size(min = 3, max = 20, message = "El código de error debe tener entre 3 y 20 caracteres")
    private String code;
    
    @NotBlank(message = "El mensaje de error no puede estar vacío")
    @Size(min = 10, max = 500, message = "El mensaje de error debe tener entre 10 y 500 caracteres")
    private String message;
}
