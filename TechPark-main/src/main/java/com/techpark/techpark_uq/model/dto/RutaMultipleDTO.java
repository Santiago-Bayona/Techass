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
public class RutaMultipleDTO {
    
    private List<Long> atraccionesIds;
    private List<RutaDTO> rutas;
    private Double distanciaTotal;
    private Integer tiempoTotal;
    private String sugerencia;
}