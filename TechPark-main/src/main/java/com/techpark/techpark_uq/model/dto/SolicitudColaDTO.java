package com.techpark.techpark_uq.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudColaDTO {
    
    @NotNull(message = "El ID del visitante es obligatorio")
    private Long visitanteId;
    
    @NotNull(message = "El ID de la atracción es obligatorio")
    private Long atraccionId;
}