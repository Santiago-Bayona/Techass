package com.techpark.techpark_uq.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudRutaDTO {
    
    @NotNull(message = "El ID de origen es obligatorio")
    private Long origenId;
    
    @NotNull(message = "El ID de destino es obligatorio")
    private Long destinoId;
    
    private String criterio;  // DISTANCIA, TIEMPO, MENOS_TRANSBORDOS (opcional)
}