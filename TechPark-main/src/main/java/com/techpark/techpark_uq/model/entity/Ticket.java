package com.techpark.techpark_uq.model.entity;


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "tickets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "visitante_id")
    private Visitante visitante;
    
    @Enumerated(EnumType.STRING)
    private TipoTicket tipo;  // GENERAL, FAMILIAR, FAST_PASS
    
    @Column(name = "fecha_compra")
    private LocalDateTime fechaCompra;
    
    @Column(name = "fecha_uso")
    private LocalDateTime fechaUso;
    
    private Double precio;
    
    private Boolean usado = false;
    
    @PrePersist
    protected void onCreate() {
        fechaCompra = LocalDateTime.now();
    }
}

enum TipoTicket {
    GENERAL, FAMILIAR, FAST_PASS
}