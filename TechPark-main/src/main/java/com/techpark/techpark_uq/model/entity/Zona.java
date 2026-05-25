package com.techpark.techpark_uq.model.entity;


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "zonas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Zona {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String nombre;  // "Zona Infantil", "Zona Extrema", etc.
    
    private String descripcion;
    
    @Column(name = "capacidad_maxima")
    private Integer capacidadMaxima;
    
    @Column(name = "aforo_actual")
    private Integer aforoActual = 0;
    
    @OneToMany(mappedBy = "zona", cascade = CascadeType.ALL)
    private List<Atraccion> atracciones = new ArrayList<>();
    
    @Column(name = "color_representativo")
    private String colorRepresentativo;  // Para el mapa visual
}