package com.techpark.techpark_uq.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

@Entity
@Table(name = "alertas_clima")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertaClima {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "tipo_alerta")
    private String tipoAlerta;
    
    private String severidad;
    
    @Column(length = 500)
    private String mensaje;
    
    @Column(name = "fecha_generacion")
    private LocalDateTime fechaGeneracion;
    
    @Column(name = "fecha_fin_estimada")
    private LocalDateTime fechaFinEstimada;
    
    private Boolean activa;
    
    @Column(name = "atracciones_afectadas")
    private String atraccionesAfectadasIds;  // JSON o IDs separados por coma
    
    @ManyToOne
    @JoinColumn(name = "operador_id")
    private Usuario operador;
    
    @Column(name = "fecha_resolucion")
    private LocalDateTime fechaResolucion;
    
    @PrePersist
    protected void onCreate() {
        fechaGeneracion = LocalDateTime.now();
        if (activa == null) {
            activa = true;
        }
    }
}