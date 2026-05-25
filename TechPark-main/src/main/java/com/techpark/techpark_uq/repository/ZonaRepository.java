package com.techpark.techpark_uq.repository;


import com.techpark.techpark_uq.model.entity.Zona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ZonaRepository extends JpaRepository<Zona, Long> {
    
    @Query("SELECT z FROM Zona z LEFT JOIN FETCH z.atracciones")
    List<Zona> findAllWithAtracciones();
    
    @Query("SELECT SUM(z.aforoActual) FROM Zona z")
    Integer calcularAforoTotalParque();
}