package com.techpark.techpark_uq.model.entity;


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "historial_visitas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistorialVisita {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "visitante_id")
    private Visitante visitante;
    
    @ManyToOne
    @JoinColumn(name = "atraccion_id")
    private Atraccion atraccion;
    
    private LocalDateTime fechaVisita;
    private Integer tiempoEsperaReal;  // minutos que realmente esperó
    private Boolean usoFastPass;  // Si usó prioridad
    
    @PrePersist
    protected void onCreate() {
        fechaVisita = LocalDateTime.now();
    }
}