package com.techpark.techpark_uq.repository;

import com.techpark.techpark_uq.model.entity.ColaVirtual;
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
public interface ColaVirtualRepository extends JpaRepository<ColaVirtual, Long> {

    /**
     * Buscar colas activas (no atendidas) de una atracción, ordenadas por prioridad
     * y hora de ingreso
     */
    List<ColaVirtual> findByAtraccionIdAndAtendidoFalseOrderByPrioridadAscHoraIngresoColaAsc(Long atraccionId);

    /**
     * Buscar todas las colas activas de una atracción
     */
    List<ColaVirtual> findByAtraccionIdAndAtendidoFalse(Long atraccionId);

    /**
     * Buscar colas atendidas de una atracción
     */
    List<ColaVirtual> findByAtraccionIdAndAtendidoTrue(Long atraccionId);

    /**
     * Buscar colas atendidas de una atracción después de una fecha
     */
    List<ColaVirtual> findByAtraccionIdAndAtendidoTrueAndFechaAtencionAfter(Long atraccionId, LocalDateTime fecha);

    /**
     * Buscar todas las colas activas de un visitante
     */
    List<ColaVirtual> findByVisitanteIdAndAtendidoFalse(Long visitanteId);

    /**
     * Buscar una cola activa específica de un visitante en una atracción
     */
    Optional<ColaVirtual> findByVisitanteIdAndAtraccionIdAndAtendidoFalse(Long visitanteId, Long atraccionId);

    /**
     * Contar cuántas personas están en la cola de una atracción
     */
    @Query("SELECT COUNT(c) FROM ColaVirtual c WHERE c.atraccion.id = :atraccionId AND c.atendido = false")
    Integer contarPersonasEnCola(@Param("atraccionId") Long atraccionId);

    /**
     * Contar cuántas personas con prioridad Fast-Pass están en la cola
     */
    @Query("SELECT COUNT(c) FROM ColaVirtual c WHERE c.atraccion.id = :atraccionId AND c.atendido = false AND c.prioridad = 1")
    Integer contarFastPassEnCola(@Param("atraccionId") Long atraccionId);

    /**
     * Contar cuántas personas con ticket General están en la cola
     */
    @Query("SELECT COUNT(c) FROM ColaVirtual c WHERE c.atraccion.id = :atraccionId AND c.atendido = false AND c.prioridad = 2")
    Integer contarGeneralEnCola(@Param("atraccionId") Long atraccionId);

    /**
     * Marcar una cola como atendida
     */
    @Modifying
    @Transactional
    @Query("UPDATE ColaVirtual c SET c.atendido = true, c.fechaAtencion = :fechaAtencion, c.tiempoEsperaReal = :tiempoEspera WHERE c.id = :id")
    void marcarComoAtendida(@Param("id") Long id,
            @Param("fechaAtencion") LocalDateTime fechaAtencion,
            @Param("tiempoEspera") Integer tiempoEspera);

    /**
     * Obtener el tiempo promedio de espera de una atracción
     */
    @Query("SELECT AVG(c.tiempoEsperaReal) FROM ColaVirtual c WHERE c.atraccion.id = :atraccionId AND c.atendido = true AND c.tiempoEsperaReal IS NOT NULL")
    Double obtenerTiempoPromedioEspera(@Param("atraccionId") Long atraccionId);

    /**
     * Obtener la posición actual de un visitante en la cola
     */
    @Query("SELECT COUNT(c) + 1 FROM ColaVirtual c WHERE c.atraccion.id = :atraccionId AND c.atendido = false AND (c.prioridad < :prioridad OR (c.prioridad = :prioridad AND c.horaIngresoCola < :horaIngreso))")
    Integer obtenerPosicionEnCola(@Param("atraccionId") Long atraccionId,
            @Param("prioridad") Integer prioridad,
            @Param("horaIngreso") LocalDateTime horaIngreso);

    /**
     * Eliminar todas las colas viejas (para limpieza)
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM ColaVirtual c WHERE c.atendido = true AND c.fechaAtencion < :fechaLimite")
    void limpiarColasAntiguas(@Param("fechaLimite") LocalDateTime fechaLimite);

    /**
     * Obtener estadísticas de cola por atracción
     */
    @Query("SELECT c.atraccion.id, COUNT(c), AVG(c.tiempoEsperaReal) " +
            "FROM ColaVirtual c WHERE c.atendido = true GROUP BY c.atraccion.id")
    List<Object[]> obtenerEstadisticasPorAtraccion();

    /**
     * Obtener todas las colas activas con información de visitante y atracción
     */
    @Query("SELECT c FROM ColaVirtual c JOIN FETCH c.visitante JOIN FETCH c.atraccion WHERE c.atendido = false ORDER BY c.prioridad ASC, c.horaIngresoCola ASC")
    List<ColaVirtual> findAllActiveWithDetails();

    // Método para obtener colas activas de un visitante
    @Query("SELECT c FROM ColaVirtual c WHERE c.visitante.id = :visitanteId AND c.atendido = false ORDER BY c.prioridad ASC, c.horaIngresoCola ASC")
    List<ColaVirtual> findColasActivasByVisitanteId(@Param("visitanteId") Long visitanteId);

    @Modifying
    @Transactional
    @Query("DELETE FROM ColaVirtual c WHERE c.visitante.id = :visitanteId")
    void deleteByVisitanteId(@Param("visitanteId") Long visitanteId);

    
}