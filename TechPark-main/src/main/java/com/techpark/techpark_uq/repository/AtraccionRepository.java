package com.techpark.techpark_uq.repository;



import com.techpark.techpark_uq.model.entity.Atraccion;
import com.techpark.techpark_uq.model.entity.EstadoAtraccion;
import com.techpark.techpark_uq.model.entity.TipoAtraccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Repository
public interface AtraccionRepository extends JpaRepository<Atraccion, Long> {
    
    List<Atraccion> findByEstado(EstadoAtraccion estado);
    
    List<Atraccion> findByTipo(TipoAtraccion tipo);
    
    List<Atraccion> findByZonaId(Long zonaId);
    
    @Query("SELECT a FROM Atraccion a WHERE a.contadorVisitantes >= 500 AND a.estado != 'MANTENIMIENTO'")
    List<Atraccion> findAtraccionesQueNecesitanMantenimiento();
    
    @Modifying
    @Transactional
    @Query("UPDATE Atraccion a SET a.contadorVisitantes = a.contadorVisitantes + 1 WHERE a.id = :atraccionId")
    void incrementarContadorVisitantes(Long atraccionId);
    
    @Query("SELECT AVG(a.tiempoEsperaEstimado) FROM Atraccion a WHERE a.estado = 'ACTIVA'")
    Double calcularTiempoEsperaPromedio();
}