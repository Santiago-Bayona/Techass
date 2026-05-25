package com.techpark.techpark_uq.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RutaDTO {
    
    private Long origenId;
    private String origenNombre;
    private Long destinoId;
    private String destinoNombre;
    private List<PasoRutaDTO> pasos;
    private Double distanciaTotal;  // en metros
    private Integer tiempoEstimadoTotal;  // en minutos
    private String mensaje;
}

