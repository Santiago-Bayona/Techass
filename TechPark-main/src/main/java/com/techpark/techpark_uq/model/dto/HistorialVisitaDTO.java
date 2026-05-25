package com.techpark.techpark_uq.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialVisitaDTO {
    private Long id;
    private Long atraccionId;
    private String atraccionNombre;
    private String atraccionTipo;
    private LocalDateTime fechaVisita;
    private Integer tiempoEsperaReal;
    private Boolean usoFastPass;
}
