package com.techpark.techpark_uq.repository;



import com.techpark.techpark_uq.model.entity.Visitante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface VisitanteRepository extends JpaRepository<Visitante, Long> {
    Optional<Visitante> findByDocumento(String documento);
    
    @Query("SELECT v FROM Visitante v WHERE v.ticketActivo IS NOT NULL")
    List<Visitante> findVisitantesConTicketActivo();
    
    @Query("SELECT COUNT(v) FROM Visitante v WHERE v.ubicacionActual = :ubicacion")
    Long countByUbicacionActual(String ubicacion);
}