package com.techpark.techpark_uq.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReporteIngresosDTO {
    
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private Double ingresosTotales;
    private Integer totalVisitantes;
    private Double promedioPorVisitante;
    
    // Desglose por tipo de ticket
    private Map<String, Double> ingresosPorTipoTicket;
    private Map<String, Integer> ticketsVendidosPorTipo;
    
    // Desglose por atracción
    private Map<String, Double> ingresosPorAtraccion;
    
    // Comparativa con período anterior
    private Double crecimientoPorcentual;
    private String tendencia;  // CRECIMIENTO, DECRECIMIENTO, ESTABLE
}
