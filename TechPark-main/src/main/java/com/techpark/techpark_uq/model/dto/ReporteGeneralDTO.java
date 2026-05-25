package com.techpark.techpark_uq.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.List; // 👈 ESTE ES EL QUE FALTA

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReporteGeneralDTO {
    
    private LocalDateTime fechaGeneracion;
    private String periodo;  // DIARIO, SEMANAL, MENSUAL
    
    // Resumen ejecutivo
    private String resumenEjecutivo;
    
    // Métricas principales
    private ReporteIngresosDTO ingresos;
    private ReporteAtraccionesDTO atracciones;
    private ReporteTiemposDTO tiempos;
    private ReporteMantenimientoDTO mantenimiento;
    private ReporteAfluenciaDTO afluencia;
    
    // Calificación general del parque (0-100)
    private Integer calificacionGeneral;
    private List<String> puntosFuertes;
    private List<String> areasMejora;
}