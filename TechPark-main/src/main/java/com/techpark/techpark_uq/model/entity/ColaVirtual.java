package com.techpark.techpark_uq.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

@Entity
@Table(name = "colas_virtuales")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ColaVirtual {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "visitante_id")
    private Visitante visitante;
    
    @ManyToOne
    @JoinColumn(name = "atraccion_id")
    private Atraccion atraccion;
    
    private Integer prioridad;  // 1 = Fast-Pass, 2 = General
    
    @Column(name = "hora_ingreso_cola")
    private LocalDateTime horaIngresoCola;
    
    private Boolean atendido;
    
    private Integer posicion;  // Posición en la cola
    
    @Column(name = "tiempo_espera_real")
    private Integer tiempoEsperaReal;  // Tiempo real que esperó en minutos
    
    @Column(name = "fecha_atencion")
    private LocalDateTime fechaAtencion;
    
    @PrePersist
    protected void onCreate() {
        if (horaIngresoCola == null) {
            horaIngresoCola = LocalDateTime.now();
        }
        if (atendido == null) {
            atendido = false;
        }
    }
}