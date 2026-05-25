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
public class ColaVirtualDTO {
    
    private Long id;
    private Long visitanteId;
    private String visitanteNombre;
    private String visitanteDocumento;
    private Long atraccionId;
    private String atraccionNombre;
    private Integer prioridad;  // 1 = Fast-Pass, 2 = General
    private Integer posicion;
    private Integer tiempoEstimadoEspera;  // en minutos
    private LocalDateTime horaIngreso;
    private Boolean atendido;
    private String estado;  // EN_COLA, ATENDIDO, CANCELADO
}