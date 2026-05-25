package com.techpark.techpark_uq.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RespuestaColaDTO {
    
    private Integer posicion;
    private Integer tiempoEstimadoEspera;
    private Integer personasDelante;
    private String mensaje;
    private Boolean tienePrioridad;  // Si es Fast-Pass
}