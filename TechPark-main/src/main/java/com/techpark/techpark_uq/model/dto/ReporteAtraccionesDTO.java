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
public class ReporteAtraccionesDTO {
    
    private List<AtraccionEstadisticasDTO> atraccionesMasVisitadas;
    private List<AtraccionEstadisticasDTO> atraccionesMenosVisitadas;
    private Double promedioVisitantesPorAtraccion;
    private Integer totalVisitasParque;
    private String atraccionEstrella;  // La más visitada
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AtraccionEstadisticasDTO {
        private Long id;
        private String nombre;
        private String zona;
        private Integer totalVisitantes;
        private Integer tiempoPromedioEspera;
        private Double porcentajeOcupacion;
        private Integer numeroMantenimientos;
        private Double popularidad;  // 0-100%
    }
}