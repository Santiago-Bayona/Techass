package com.techpark.techpark_uq.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RevisionTecnicaDTO {
    
    @NotNull(message = "El ID de la atracción es obligatorio")
    private Long atraccionId;
    
    @NotNull(message = "El ID del operador es obligatorio")
    private Long operadorId;
    
    @NotBlank(message = "El comentario de la revisión es obligatorio")
    private String comentario;
    
    private Boolean mantenimientoExitoso;
    private Integer nuevosVisitantesPermitidos;  // Cuántos más puede recibir antes del próximo mantenimiento
}