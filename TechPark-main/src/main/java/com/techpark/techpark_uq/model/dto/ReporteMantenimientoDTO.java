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
public class ReporteMantenimientoDTO {
    
    private Integer totalMantenimientos;
    private Integer mantenimientosPendientes;
    private Integer mantenimientosCompletados;
    private Double tiempoPromedioResolucionMinutos;
    
    // Por tipo
    private Map<String, Integer> mantenimientosPorTipo;  // PREVENTIVA, CORRECTIVA
    private Map<String, Integer> mantenimientosPorPrioridad;  // ALTA, MEDIA, BAJA
    
    // Top atracciones con más incidentes
    private List<AtraccionIncidentesDTO> atraccionesMasIncidentes;
    
    // Alertas generadas por clima
    private Integer alertasClimaGeneradas;
    private Map<String, Integer> cierresPorCausa;  // clima, mantenimiento, operativo
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AtraccionIncidentesDTO {
        private String nombre;
        private Integer incidentes;
        private Double tiempoPromedioResolucion;
        private String estadoActual;
    }
}