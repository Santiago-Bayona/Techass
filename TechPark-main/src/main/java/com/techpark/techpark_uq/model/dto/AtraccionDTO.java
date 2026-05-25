package com.techpark.techpark_uq.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AtraccionDTO {

    private Long id;
    @NotBlank(message = "el nombre de la atraccion es obligatoria")
    private String nombre;

    @NotNull(message = "El tipo de la atraccion debe de ser obligatoria")
    private String tipo; // MECANUCA, ACUATICA, INFANTIL, SHOW

    @NotNull(message = "La capacidad maxima es obligatoria")
    private Integer capacidadMaxima;

    @NotNull(message = "La altura mínima es obligatoria")
    private Double alturaMinima;

    @NotNull(message = "La edad mínima es obligatoria")
    private Integer edadMinima;

    private Double costoAdicional;
    private Integer contadorVisitantes;
    private Integer tiempoEsperaEstimado;
    private String estado; // ACTIVA, MANTENIMIENTO, CERRADA
    private String motivoCierre;
    private Long zonaId;
    private String zonaNombre;
    private Double posicionX;
    private Double posicionY;

}
