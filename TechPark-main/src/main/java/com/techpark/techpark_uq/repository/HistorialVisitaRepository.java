package com.techpark.techpark_uq.repository;


import com.techpark.techpark_uq.model.entity.HistorialVisita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HistorialVisitaRepository extends JpaRepository<HistorialVisita, Long> {
    
    List<HistorialVisita> findByVisitanteIdOrderByFechaVisitaDesc(Long visitanteId);
    
    @Query("SELECT h.atraccion.id, COUNT(h) FROM HistorialVisita h GROUP BY h.atraccion.id ORDER BY COUNT(h) DESC")
    List<Object[]> findAtraccionesMasVisitadas();
    
    @Query("SELECT AVG(h.tiempoEsperaReal) FROM HistorialVisita h WHERE h.fechaVisita BETWEEN :inicio AND :fin")
    Double findTiempoEsperaPromedioPorFecha(LocalDateTime inicio, LocalDateTime fin);
}