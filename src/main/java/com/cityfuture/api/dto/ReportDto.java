package com.cityfuture.api.dto;

import lombok.Data;
import jakarta.validation.constraints.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

@Data
public class ReportDto {
    
    @Valid
    @NotNull(message = "La lista de construcciones pendientes no puede ser nula")
    private List<@NotBlank(message = "Los nombres de construcciones pendientes no pueden estar vacíos") String> pendingConstructions;
    
    @Valid
    @NotNull(message = "El mapa de construcciones finalizadas por tipo no puede ser nulo")
    private Map<@NotBlank(message = "Los tipos de construcción no pueden estar vacíos") String, 
               @NotNull(message = "Las cantidades no pueden ser nulas") 
               @Min(value = 0, message = "Las cantidades deben ser mayor o igual a 0") Integer> finishedByType;
    
    @Valid
    @NotNull(message = "El mapa de construcciones en progreso por tipo no puede ser nulo")
    private Map<@NotBlank(message = "Los tipos de construcción no pueden estar vacíos") String, 
               @NotNull(message = "Las cantidades no pueden ser nulas") 
               @Min(value = 0, message = "Las cantidades deben ser mayor o igual a 0") Integer> inProgressByType;
    
    @NotBlank(message = "La fecha de finalización del proyecto no puede estar vacía")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "La fecha debe tener el formato YYYY-MM-DD")
    private String projectEndDate;
}
