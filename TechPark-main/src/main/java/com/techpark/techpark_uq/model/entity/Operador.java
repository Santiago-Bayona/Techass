package com.techpark.techpark_uq.model.entity;



import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "operadores")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
public class Operador extends Usuario {
    
    @ManyToOne
    @JoinColumn(name = "zona_asignada_id")
    private Zona zonaAsignada;  // Zona que supervisa
    
    @Column(name = "numero_empleado", unique = true)
    private String numeroEmpleado;
    
    @Column(name = "turno")
    private String turno;  // MAÑANA, TARDE, NOCHE
}