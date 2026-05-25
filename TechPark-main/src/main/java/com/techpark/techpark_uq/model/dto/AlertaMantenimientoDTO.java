package com.techpark.techpark_uq.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertaMantenimientoDTO {
    
    private Long id;
    private Long atraccionId;
    private String atraccionNombre;
    private Integer visitantesAcumulados;
    private String tipoAlerta;  // PREVENTIVA, CORRECTIVA, URGENTE
    private String prioridad;   // ALTA, MEDIA, BAJA
    private String estado;      // PENDIENTE, EN_PROCESO, RESUELTA
    private String descripcion;
    private LocalDateTime fechaGeneracion;
    private LocalDateTime fechaResolucion;
    private Long operadorId;     // Operador que resolvió
    private String operadorNombre;
}