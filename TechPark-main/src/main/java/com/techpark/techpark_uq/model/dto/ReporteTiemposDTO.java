package com.techpark.techpark_uq.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReporteTiemposDTO {
    
    private Double tiempoPromedioGeneral;
    private Integer tiempoMaximoRegistrado;
    private Integer tiempoMinimoRegistrado;
    
    // Por atracción
    private Map<String, Double> tiemposPromedioPorAtraccion;
    
    // Por hora del día (para identificar horas pico)
    private Map<Integer, Double> tiemposPorHora;
    
    // Tiempos de espera por tipo de ticket
    private Map<String, Double> tiemposPorTipoTicket;  // Fast-Pass vs General
    
    private List<AtraccionCuelloBotellaDTO> cuellosDeBotella;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AtraccionCuelloBotellaDTO {
        private String nombre;
        private Integer tiempoPromedio;
        private Integer capacidad;
        private Integer visitantesPorHora;
        private String recomendacion;
    }
}