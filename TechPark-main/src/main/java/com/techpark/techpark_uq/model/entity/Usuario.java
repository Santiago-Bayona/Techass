package com.techpark.techpark_uq.model.entity;


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
@Inheritance(strategy = InheritanceType.JOINED)  // Herencia: cada tipo tiene su tabla
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Usuario {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String nombre;
    
    @Column(nullable = false, unique = true, length = 20)
    private String documento;
    
    @Column(nullable = false, unique = true, length = 100)
    private String email;
    
    @Column(nullable = false)
    private String password;  // Encriptada
    
    @Column(nullable = false)
    private Integer edad;
    
    @Column(nullable = false)
    private Double estatura;  // en metros
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RolUsuario rol;  // VISITANTE, OPERADOR, ADMINISTRADOR
    
    @Column(name = "foto_url")
    private String fotoUrl;  // Opcional
    
    @Column(name = "saldo_virtual")
    private Double saldoVirtual;  // Solo para visitantes
    
    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;
    
    private Boolean activo = true;
    
    @PrePersist
    protected void onCreate() {
        fechaRegistro = LocalDateTime.now();
        if (saldoVirtual == null) saldoVirtual = 0.0;
    }
}

