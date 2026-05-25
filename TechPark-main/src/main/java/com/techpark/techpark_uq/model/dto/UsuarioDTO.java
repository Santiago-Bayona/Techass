package com.techpark.techpark_uq.model.dto;

import lombok.Data;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UsuarioDTO {
    private Long id;

    @NotBlank(message = "el nombre es obligatorio")
    @Size(min = 2, max = 100, message = "el  nombre debe tener entre 2 y 100  caracteres")
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

    private String rol; // VISITANTE, OPERADOR, ADMINISTRADOR
    private String fotoUrl;
    private Double saldoVirtual;
    private boolean activo;
    private String ticketActivo;

}
