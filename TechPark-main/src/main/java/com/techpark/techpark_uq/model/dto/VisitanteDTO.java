package com.techpark.techpark_uq.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import java.util.Set;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
public class VisitanteDTO extends UsuarioDTO {
    
    private String ticketActivo;  // GENERAL, FAMILIAR, FAST_PASS
    private Set<Long> atraccionesFavoritas;  // IDs de atracciones favoritas
    private List<HistorialVisitaDTO> historialVisitas;
    private String ubicacionActual;
}