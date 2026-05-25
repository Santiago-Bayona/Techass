package com.techpark.techpark_uq.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertaClimaDTO {
    
    private Long id;
    private String tipoAlerta;      // TORMENTA_ELECTRICA, LLUVIA_FUERTE, VIENTO_FUERTE, CALOR_EXTREMO
    private String severidad;        // ALTA, MEDIA, BAJA
    private String mensaje;
    private LocalDateTime fechaGeneracion;
    private LocalDateTime fechaFinEstimada;
    private Boolean activa;
    private List<Long> atraccionesAfectadasIds;
    private List<String> atraccionesAfectadasNombres;
    private Long operadorId;         // Administrador que activó la alerta
    private String operadorNombre;
}