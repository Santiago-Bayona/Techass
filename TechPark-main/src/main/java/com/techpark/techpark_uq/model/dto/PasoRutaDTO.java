package com.techpark.techpark_uq.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasoRutaDTO {

    private Long atraccionId;
    private String atraccionNombre;
    private String tipoAtraccion;
    private Double distanciaDesdeAnterior; // en metros
    private Integer tiempoEstimado; // en minutos
    private String instruccion; // Ej: "Gira a la izquierda", "Sigue derecho"
    private Integer orden;
}