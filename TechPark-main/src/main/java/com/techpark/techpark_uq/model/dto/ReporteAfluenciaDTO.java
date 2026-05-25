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
public class ReporteAfluenciaDTO {
    
    private Integer totalVisitantesDia;
    private Integer capacidadMaximaParque;
    private Double porcentajeOcupacion;
    private String nivelAfluencia;  // BAJA, MEDIA, ALTA, LLENO
    
    // Por zona
    private Map<String, Integer> visitantesPorZona;
    private Map<String, Double> ocupacionPorZona;
    
    // Por hora (para identificar horas pico)
    private Map<Integer, Integer> visitantesPorHora;
    private List<Integer> horasPico;
    
    // Tendencia (comparado con días anteriores)
    private Double tendenciaPorcentual;
    
    // Recomendaciones para el administrador
    private List<String> recomendaciones;
}