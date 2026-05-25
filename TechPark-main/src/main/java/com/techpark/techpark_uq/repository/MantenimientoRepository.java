package com.techpark.techpark_uq.repository;

import com.techpark.techpark_uq.model.entity.Mantenimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MantenimientoRepository extends JpaRepository<Mantenimiento, Long> {
    
    /**
     * Buscar alertas de mantenimiento por atracción
     */
    List<Mantenimiento> findByAtraccionIdOrderByFechaGeneracionDesc(Long atraccionId);
    
    /**
     * Buscar alertas pendientes
     */
    List<Mantenimiento> findByEstadoOrderByPrioridadAscFechaGeneracionAsc(String estado);
    
    /**
     * Buscar alertas por prioridad
     */
    List<Mantenimiento> findByPrioridadAndEstado(String prioridad, String estado);
    
    /**
     * Buscar alertas no resueltas de una atracción específica
     */
    Optional<Mantenimiento> findByAtraccionIdAndEstadoNot(Long atraccionId, String estado);
    
    /**
     * Contar alertas pendientes por prioridad
     */
    @Query("SELECT m.prioridad, COUNT(m) FROM Mantenimiento m WHERE m.estado = 'PENDIENTE' GROUP BY m.prioridad")
    List<Object[]> countAlertasPorPrioridad();
    
    /**
     * Obtener tiempo promedio de resolución
     */
    @Query("SELECT AVG(m.tiempoResolucionMinutos) FROM Mantenimiento m WHERE m.estado = 'RESUELTA'")
    Double obtenerTiempoPromedioResolucion();
    
    /**
     * Obtener atracciones con más incidentes
     */
    @Query("SELECT m.atraccion.nombre, COUNT(m) FROM Mantenimiento m GROUP BY m.atraccion.id ORDER BY COUNT(m) DESC")
    List<Object[]> findAtraccionesConMasIncidentes();
    
    /**
     * Marcar alerta como resuelta
     */
    @Modifying
    @Transactional
    @Query("UPDATE Mantenimiento m SET m.estado = 'RESUELTA', m.fechaResolucion = :fecha, " +
           "m.operador = :operadorId, m.comentarioResolucion = :comentario, " +
           "m.tiempoResolucionMinutos = :tiempo WHERE m.id = :id")
    void resolverAlerta(@Param("id") Long id, 
                        @Param("fecha") LocalDateTime fecha,
                        @Param("operadorId") Long operadorId,
                        @Param("comentario") String comentario,
                        @Param("tiempo") Integer tiempo);
    
    /**
     * Obtener alertas generadas en un período
     */
    List<Mantenimiento> findByFechaGeneracionBetween(LocalDateTime inicio, LocalDateTime fin);
}