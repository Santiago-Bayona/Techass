package com.techpark.techpark_uq.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

@Entity
@Table(name = "mantenimientos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mantenimiento {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "atraccion_id")
    private Atraccion atraccion;
    
    @Column(name = "visitantes_acumulados")
    private Integer visitantesAcumulados;  // Cuántos visitantes tenía cuando se generó la alerta
    
    @Column(name = "tipo_alerta")
    private String tipoAlerta;  // PREVENTIVA, CORRECTIVA, URGENTE
    
    private String prioridad;   // ALTA, MEDIA, BAJA
    
    private String estado;      // PENDIENTE, EN_PROCESO, RESUELTA
    
    @Column(length = 500)
    private String descripcion;
    
    @Column(name = "fecha_generacion")
    private LocalDateTime fechaGeneracion;
    
    @Column(name = "fecha_resolucion")
    private LocalDateTime fechaResolucion;
    
    @ManyToOne
    @JoinColumn(name = "operador_id")
    private Usuario operador;  // Operador que resolvió el mantenimiento
    
    @Column(name = "comentario_resolucion", length = 500)
    private String comentarioResolucion;
    
    @Column(name = "tiempo_resolucion_minutos")
    private Integer tiempoResolucionMinutos;  // Tiempo que tomó resolver
    
    @PrePersist
    protected void onCreate() {
        fechaGeneracion = LocalDateTime.now();
        if (estado == null) {
            estado = "PENDIENTE";
        }
    }
}