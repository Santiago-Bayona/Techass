package com.techpark.techpark_uq.model.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegistroVisitanteRequest {
    
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;
    
    @NotBlank(message = "El documento es obligatorio")
    @Pattern(regexp = "\\d{8,10}", message = "El documento debe tener entre 8 y 10 dígitos")
    private String documento;
    
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe ser válido")
    private String email;
    
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;
    
    @NotNull(message = "La edad es obligatoria")
    @Min(value = 0, message = "La edad no puede ser negativa")
    @Max(value = 120, message = "La edad no puede ser mayor a 120")
    private Integer edad;
    
    @NotNull(message = "La estatura es obligatoria")
    @DecimalMin(value = "0.5", message = "La estatura mínima es 0.5m")
    @DecimalMax(value = "2.5", message = "La estatura máxima es 2.5m")
    private Double estatura;
    
    @NotBlank(message = "El tipo de ticket es obligatorio")
    @Pattern(regexp = "GENERAL|FAMILIAR|FAST_PASS", message = "Ticket debe ser GENERAL, FAMILIAR o FAST_PASS")
    private String tipoTicket;
    
    private String fotoUrl;  // Opcional
}