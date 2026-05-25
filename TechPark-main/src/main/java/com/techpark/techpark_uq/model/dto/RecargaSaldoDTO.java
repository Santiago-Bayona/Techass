package com.techpark.techpark_uq.model.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RecargaSaldoDTO {
    
    @NotNull(message = "El ID del usuario es obligatorio")
    private Long usuarioId;
    
    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "1.0", message = "El monto mínimo es 1.0")
    @DecimalMax(value = "500.0", message = "El monto máximo es 500.0")
    private Double monto;
    
    private String metodoPago; // TARJETA, EFECTIVO, PSE (opcional)
}