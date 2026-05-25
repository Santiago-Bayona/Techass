package com.techpark.techpark_uq.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "visitantes")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder

public class Visitante  extends Usuario{
    
    @Column(name = "ticket_activo")
    private String ticketActivo;  // GENERAL, FAMILIAR, FAST_PASS
    
    @ElementCollection
    @CollectionTable(name = "visitante_favoritos", 
                     joinColumns = @JoinColumn(name = "visitante_id"))
    @Column(name = "atraccion_id")
    private Set<Long> atraccionesFavoritas = new HashSet<>();  // Usando SET
    
    @OneToMany(mappedBy = "visitante", cascade = CascadeType.ALL)
    private List<HistorialVisita> historialVisitas = new ArrayList<>();  // Lista Enlazada propia después
    
    @Column(name = "ubicacion_actual")
    private String ubicacionActual; 


}
