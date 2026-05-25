package com.techpark.techpark_uq.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimulacionClimaDTO {
    
    @NotBlank(message = "El tipo de clima es obligatorio")
    private String tipoClima;  // NORMAL, TORMENTA_ELECTRICA, LLUVIA_FUERTE, VIENTO_FUERTE
    
    private String severidad;  // ALTA, MEDIA, BAJA
    
    @Min(value = 5, message = "La duración mínima es 5 minutos")
    @Max(value = 180, message = "La duración máxima es 180 minutos")
    private Integer duracionEstimadaMinutos;
    
    private String comentario;
}