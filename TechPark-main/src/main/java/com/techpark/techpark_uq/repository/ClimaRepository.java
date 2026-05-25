package com.techpark.techpark_uq.repository;

import com.techpark.techpark_uq.model.entity.AlertaClima;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ClimaRepository extends JpaRepository<AlertaClima, Long> {
    
    /**
     * Buscar alertas climáticas activas
     */
    List<AlertaClima> findByActivaTrueOrderBySeveridadDescFechaGeneracionDesc();
    
    /**
     * Buscar alertas por tipo
     */
    List<AlertaClima> findByTipoAlertaAndActivaTrue(String tipoAlerta);
    
    /**
     * Buscar alertas históricas
     */
    List<AlertaClima> findByActivaFalseOrderByFechaGeneracionDesc();
    
    /**
     * Buscar alertas en un período
     */
    List<AlertaClima> findByFechaGeneracionBetween(LocalDateTime inicio, LocalDateTime fin);
    
    /**
     * Desactivar alerta
     */
    @Modifying
    @Transactional
    @Query("UPDATE AlertaClima a SET a.activa = false, a.fechaResolucion = :fecha WHERE a.id = :id")
    void desactivarAlerta(@Param("id") Long id, @Param("fecha") LocalDateTime fecha);
    
    /**
     * Contar alertas por tipo
     */
    @Query("SELECT a.tipoAlerta, COUNT(a) FROM AlertaClima a GROUP BY a.tipoAlerta")
    List<Object[]> countAlertasPorTipo();
}