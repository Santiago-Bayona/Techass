package com.techpark.techpark_uq.model.entity;


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "atracciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Atraccion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String nombre;
    
    @Enumerated(EnumType.STRING)
    private TipoAtraccion tipo;  // MECANICA, ACUATICA, INFANTIL, SHOW
    
    @Column(name = "capacidad_maxima")
    private Integer capacidadMaxima;  // Por ciclo
    
    @Column(name = "altura_minima")
    private Double alturaMinima;  // en metros
    
    @Column(name = "edad_minima")
    private Integer edadMinima;
    
    @Column(name = "costo_adicional")
    private Double costoAdicional;  // Para tickets General
    
    @Column(name = "contador_visitantes")
    private Integer contadorVisitantes = 0;  // IMPORTANTE: Para mantenimiento cada 500
    
    @Column(name = "tiempo_espera_estimado")
    private Integer tiempoEsperaEstimado;  // en minutos
    
    @Enumerated(EnumType.STRING)
    private EstadoAtraccion estado;  // ACTIVA, MANTENIMIENTO, CERRADA
    
    private String motivoCierre;  // Si está cerrada
    
    @ManyToOne
    @JoinColumn(name = "zona_id")
    private Zona zona;
    
    @Column(name = "posicion_x")
    private Double posicionX;  // Para el mapa (grafo)
    
    @Column(name = "posicion_y")
    private Double posicionY;  // Para el mapa (grafo)
    
    @OneToMany(mappedBy = "atraccion")
    private List<ColaVirtual> colaVirtual = new ArrayList<>();
}



