package com.techpark.techpark_uq.service;

import com.techpark.techpark_uq.estructuras.ColaPrioridad;
import com.techpark.techpark_uq.exception.BusinessException;
import com.techpark.techpark_uq.model.dto.AlertaMantenimientoDTO;
import com.techpark.techpark_uq.model.dto.RevisionTecnicaDTO;
import com.techpark.techpark_uq.model.entity.*;
import com.techpark.techpark_uq.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servicio de Mantenimiento Preventivo
 * Bloquea atracciones al alcanzar 500 visitantes acumulados
 * Usa ColaPrioridad para gestionar alertas por prioridad
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MantenimientoService {
    
    private final AtraccionRepository atraccionRepository;
    private final MantenimientoRepository mantenimientoRepository;
    private final UsuarioRepository usuarioRepository;
    
    // Cola de prioridad para alertas de mantenimiento (ALTA prioridad primero)
    private final ColaPrioridad<AlertaMantenimiento> colaAlertas = new ColaPrioridad<>();
    
    // Mapa en memoria para seguimiento de alertas activas
    private final Map<Long, AlertaMantenimiento> alertasActivas = new ConcurrentHashMap<>();
    
    // Constantes
    private static final int LIMITE_VISITANTES_MANTENIMIENTO = 500;
    private static final int LIMITE_VISITANTES_ALERTA_TEMPRANA = 450;
    
    // ============= CLASE INTERNA =============
    
    /**
     * Alerta de mantenimiento para la cola de prioridad
     */
    @lombok.Getter
    @lombok.AllArgsConstructor
    public static class AlertaMantenimiento {
        private final Long atraccionId;
        private final String atraccionNombre;
        private final Integer visitantesActuales;
        private final LocalDateTime fechaGeneracion;
        private final String prioridad;  // ALTA, MEDIA, BAJA
        
        /**
         * Determina prioridad numérica para ColaPrioridad
         * ALTA = 1 (más urgente)
         * MEDIA = 2
         * BAJA = 3
         */
        public int obtenerNivelPrioridad() {
            switch (prioridad) {
                case "ALTA": return 1;
                case "MEDIA": return 2;
                default: return 3;
            }
        }
        
        @Override
        public String toString() {
            return String.format("⚠️ [%s] %s - %d visitantes (%s)", 
                prioridad, atraccionNombre, visitantesActuales, fechaGeneracion);
        }
    }
    
    // ============= MÉTODOS PRINCIPALES =============
    
    /**
     * Verificar automáticamente si una atracción necesita mantenimiento
     * Este método debe llamarse después de cada visita a una atracción
     */
    @Transactional
    public void verificarMantenimiento(Long atraccionId) {
        Atraccion atraccion = atraccionRepository.findById(atraccionId)
            .orElseThrow(() -> new BusinessException("Atracción no encontrada", "ATRACCION_NO_ENCONTRADA"));
        
        int visitantes = atraccion.getContadorVisitantes();
        
        log.info("Verificando mantenimiento para {}: {} visitantes", atraccion.getNombre(), visitantes);
        
        // Caso 1: Alcanzó exactamente 500 - MANTENIMIENTO OBLIGATORIO
        if (visitantes >= LIMITE_VISITANTES_MANTENIMIENTO && 
            atraccion.getEstado() != EstadoAtraccion.MANTENIMIENTO) {
            
            generarAlertaMantenimiento(atraccion, "ALTA", 
                String.format("¡MANTENIMIENTO OBLIGATORIO! La atracción ha alcanzado %d visitantes. " +
                             "Debe ser revisada antes de continuar.", visitantes));
            
            // Bloquear la atracción automáticamente
            atraccion.setEstado(EstadoAtraccion.MANTENIMIENTO);
            atraccion.setMotivoCierre(String.format("Mantenimiento preventivo - %d visitantes alcanzados", visitantes));
            atraccionRepository.save(atraccion);
            
            log.warn("🔒 Atracción {} BLOQUEADA por mantenimiento. Visitantes: {}", atraccion.getNombre(), visitantes);
        }
        // Caso 2: Alerta temprana a los 450 visitantes
        else if (visitantes >= LIMITE_VISITANTES_ALERTA_TEMPRANA && 
                 visitantes < LIMITE_VISITANTES_MANTENIMIENTO &&
                 !alertaYaGeneradaParaRango(atraccionId, LIMITE_VISITANTES_ALERTA_TEMPRANA)) {
            
            generarAlertaMantenimiento(atraccion, "MEDIA", 
                String.format("⚠️ ALERTA TEMPRANA: La atracción tiene %d visitantes. " +
                             "Faltan %d para mantenimiento obligatorio.", 
                             visitantes, LIMITE_VISITANTES_MANTENIMIENTO - visitantes));
            
            log.info("⚠️ Alerta temprana para {}: {} visitantes", atraccion.getNombre(), visitantes);
        }
    }
    
    /**
     * Registrar una revisión técnica satisfactoria
     * Esto resetea el contador y reactiva la atracción
     */
    @Transactional
    public AlertaMantenimientoDTO registrarRevisionTecnica(RevisionTecnicaDTO revision) {
        log.info("Registrando revisión técnica para atracción {} por operador {}", 
                 revision.getAtraccionId(), revision.getOperadorId());
        
        Atraccion atraccion = atraccionRepository.findById(revision.getAtraccionId())
            .orElseThrow(() -> new BusinessException("Atracción no encontrada", "ATRACCION_NO_ENCONTRADA"));
        
        Usuario operador = usuarioRepository.findById(revision.getOperadorId())
            .orElseThrow(() -> new BusinessException("Operador no encontrado", "OPERADOR_NO_ENCONTRADO"));
        
        // Validar que el operador tenga rol de OPERADOR o ADMINISTRADOR
        if (operador.getRol() != RolUsuario.OPERADOR && operador.getRol() != RolUsuario.ADMINISTRADOR) {
            throw new BusinessException("Solo los operadores o administradores pueden registrar revisiones", 
                                       "ROL_NO_AUTORIZADO");
        }
        
        // Buscar alerta activa
        Optional<Mantenimiento> alertaOpt = mantenimientoRepository
            .findByAtraccionIdAndEstadoNot(revision.getAtraccionId(), "RESUELTA");
        
        Mantenimiento mantenimiento;
        if (alertaOpt.isPresent()) {
            mantenimiento = alertaOpt.get();
            mantenimiento.setEstado("RESUELTA");
            mantenimiento.setFechaResolucion(LocalDateTime.now());
            mantenimiento.setOperador(operador);
            mantenimiento.setComentarioResolucion(revision.getComentario());
            
            // Calcular tiempo de resolución
            long minutos = ChronoUnit.MINUTES.between(mantenimiento.getFechaGeneracion(), LocalDateTime.now());
            mantenimiento.setTiempoResolucionMinutos((int) minutos);
            
            mantenimientoRepository.save(mantenimiento);
        } else {
            // Crear registro de mantenimiento preventivo
            mantenimiento = Mantenimiento.builder()
                .atraccion(atraccion)
                .visitantesAcumulados(atraccion.getContadorVisitantes())
                .tipoAlerta("PREVENTIVA")
                .prioridad("BAJA")
                .estado("RESUELTA")
                .descripcion("Mantenimiento preventivo programado")
                .fechaGeneracion(LocalDateTime.now().minusMinutes(5))
                .fechaResolucion(LocalDateTime.now())
                .operador(operador)
                .comentarioResolucion(revision.getComentario())
                .tiempoResolucionMinutos(5)
                .build();
            mantenimiento = mantenimientoRepository.save(mantenimiento);
        }
        
        // RESETEAR el contador de visitantes (regla del sistema)
        atraccion.setContadorVisitantes(0);
        atraccion.setEstado(EstadoAtraccion.ACTIVA);
        atraccion.setMotivoCierre(null);
        atraccionRepository.save(atraccion);
        
        // Eliminar de alertas activas
        alertasActivas.remove(atraccion.getId());
        
        // Eliminar de la cola de prioridad si está presente
        limpiarAlertaDeCola(atraccion.getId());
        
        log.info("✅ Revisión técnica completada para {}. Contador resetado a 0. Atracción REACTIVADA.", 
                 atraccion.getNombre());
        
        return convertirADTO(mantenimiento);
    }
    
    /**
     * Obtener la siguiente alerta de mantenimiento más prioritaria
     */
    public AlertaMantenimiento obtenerSiguienteAlerta() {
        if (colaAlertas.estaVacia()) {
            throw new BusinessException("No hay alertas de mantenimiento pendientes", "SIN_ALERTAS");
        }
        return colaAlertas.verPrimero();
    }
    
    /**
     * Procesar y eliminar la siguiente alerta prioritaria
     */
    public AlertaMantenimiento procesarSiguienteAlerta() {
        if (colaAlertas.estaVacia()) {
            throw new BusinessException("No hay alertas de mantenimiento pendientes", "SIN_ALERTAS");
        }
        return colaAlertas.desencolar();
    }
    
    /**
     * Obtener todas las alertas pendientes ordenadas por prioridad
     */
    public List<AlertaMantenimientoDTO> obtenerAlertasPendientes() {
        List<Mantenimiento> alertas = mantenimientoRepository.findByEstadoOrderByPrioridadAscFechaGeneracionAsc("PENDIENTE");
        
        return alertas.stream()
            .map(this::convertirADTO)
            .toList();
    }
    
    /**
     * Obtener alertas activas (las que están en la cola de prioridad)
     */
    public List<AlertaMantenimiento> obtenerAlertasActivasEnCola() {
        List<AlertaMantenimiento> alertas = new ArrayList<>();
        
        // Extraer temporalmente para no modificar la cola
        List<AlertaMantenimiento> tempList = new ArrayList<>();
        while (!colaAlertas.estaVacia()) {
            AlertaMantenimiento alerta = colaAlertas.desencolar();
            alertas.add(alerta);
            tempList.add(alerta);
        }
        
        // Reconstruir la cola
        for (AlertaMantenimiento alerta : tempList) {
            colaAlertas.encolar(alerta, alerta.obtenerNivelPrioridad());
        }
        
        return alertas;
    }
    
    // ============= MÉTODOS DE REPORTES =============
    
    /**
     * Obtener estadísticas de mantenimiento
     */
    public Map<String, Object> obtenerEstadisticasMantenimiento() {
        Map<String, Object> estadisticas = new HashMap<>();
        
        // Total de mantenimientos realizados
        long totalMantenimientos = mantenimientoRepository.count();
        estadisticas.put("totalMantenimientos", totalMantenimientos);
        
        // Alertas pendientes
        List<Mantenimiento> pendientes = mantenimientoRepository.findByEstadoOrderByPrioridadAscFechaGeneracionAsc("PENDIENTE");
        estadisticas.put("alertasPendientes", pendientes.size());
        
        // Alertas por prioridad
        List<Object[]> porPrioridad = mantenimientoRepository.countAlertasPorPrioridad();
        Map<String, Long> prioridadCount = new HashMap<>();
        for (Object[] row : porPrioridad) {
            prioridadCount.put((String) row[0], (Long) row[1]);
        }
        estadisticas.put("alertasPorPrioridad", prioridadCount);
        
        // Tiempo promedio de resolución
        Double tiempoPromedio = mantenimientoRepository.obtenerTiempoPromedioResolucion();
        estadisticas.put("tiempoPromedioResolucionMinutos", 
                         tiempoPromedio != null ? Math.round(tiempoPromedio) : 0);
        
        // Atracciones con más incidentes
        List<Object[]> topIncidentes = mantenimientoRepository.findAtraccionesConMasIncidentes();
        List<Map<String, Object>> topAtracciones = new ArrayList<>();
        for (int i = 0; i < Math.min(5, topIncidentes.size()); i++) {
            Object[] row = topIncidentes.get(i);
            Map<String, Object> item = new HashMap<>();
            item.put("nombre", row[0]);
            item.put("incidentes", row[1]);
            topAtracciones.add(item);
        }
        estadisticas.put("atraccionesMasIncidentes", topAtracciones);
        
        // Alertas en cola de prioridad
        estadisticas.put("alertasEnColaPrioritaria", colaAlertas.getTamanio());
        
        return estadisticas;
    }
    
    /**
     * Obtener historial de mantenimiento de una atracción
     */
    public List<AlertaMantenimientoDTO> obtenerHistorialPorAtraccion(Long atraccionId) {
        List<Mantenimiento> historial = mantenimientoRepository.findByAtraccionIdOrderByFechaGeneracionDesc(atraccionId);
        return historial.stream()
            .map(this::convertirADTO)
            .toList();
    }
    
    /**
     * Verificar qué atracciones necesitan atención inmediata
     */
    public List<Atraccion> obtenerAtraccionesEnRiesgo() {
        return atraccionRepository.findAll().stream()
            .filter(a -> a.getContadorVisitantes() >= LIMITE_VISITANTES_ALERTA_TEMPRANA 
                      && a.getEstado() == EstadoAtraccion.ACTIVA)
            .toList();
    }
    
    // ============= MÉTODOS PRIVADOS =============
    
    /**
     * Genera una nueva alerta de mantenimiento
     */
    private void generarAlertaMantenimiento(Atraccion atraccion, String prioridad, String descripcion) {
        log.info("Generando alerta {} para {}: {}", prioridad, atraccion.getNombre(), descripcion);
        
        // Crear alerta en memoria
        AlertaMantenimiento alerta = new AlertaMantenimiento(
            atraccion.getId(),
            atraccion.getNombre(),
            atraccion.getContadorVisitantes(),
            LocalDateTime.now(),
            prioridad
        );
        
        // Agregar a cola de prioridad (las ALTA salen primero)
        colaAlertas.encolar(alerta, alerta.obtenerNivelPrioridad());
        alertasActivas.put(atraccion.getId(), alerta);
        
        // Guardar en base de datos
        Mantenimiento mantenimiento = Mantenimiento.builder()
            .atraccion(atraccion)
            .visitantesAcumulados(atraccion.getContadorVisitantes())
            .tipoAlerta(prioridad.equals("ALTA") ? "CORRECTIVA" : "PREVENTIVA")
            .prioridad(prioridad)
            .estado("PENDIENTE")
            .descripcion(descripcion)
            .fechaGeneracion(LocalDateTime.now())
            .build();
        
        mantenimientoRepository.save(mantenimiento);
        
        // Log según prioridad
        if ("ALTA".equals(prioridad)) {
            log.error("🚨 ALERTA CRÍTICA: {} requiere mantenimiento URGENTE!", atraccion.getNombre());
        } else {
            log.warn("⚠️ Alerta de mantenimiento para {}", atraccion.getNombre());
        }
    }
    
    /**
     * Verifica si ya se generó una alerta para cierto rango de visitantes
     */
    private boolean alertaYaGeneradaParaRango(Long atraccionId, int rangoMinimo) {
        return mantenimientoRepository.findByAtraccionIdOrderByFechaGeneracionDesc(atraccionId)
            .stream()
            .anyMatch(m -> m.getVisitantesAcumulados() >= rangoMinimo && 
                          m.getEstado().equals("PENDIENTE"));
    }
    
    /**
     * Limpia una alerta específica de la cola de prioridad
     */
    private void limpiarAlertaDeCola(Long atraccionId) {
        // Reconstruir la cola sin la alerta específica
        ColaPrioridad<AlertaMantenimiento> nuevaCola = new ColaPrioridad<>();
        
        while (!colaAlertas.estaVacia()) {
            AlertaMantenimiento alerta = colaAlertas.desencolar();
            if (!alerta.getAtraccionId().equals(atraccionId)) {
                nuevaCola.encolar(alerta, alerta.obtenerNivelPrioridad());
            }
        }
        
        // Reemplazar la cola
        while (!nuevaCola.estaVacia()) {
            AlertaMantenimiento alerta = nuevaCola.desencolar();
            colaAlertas.encolar(alerta, alerta.obtenerNivelPrioridad());
        }
        
        alertasActivas.remove(atraccionId);
    }
    
    /**
     * Convierte entidad a DTO
     */
    private AlertaMantenimientoDTO convertirADTO(Mantenimiento mantenimiento) {
        return AlertaMantenimientoDTO.builder()
            .id(mantenimiento.getId())
            .atraccionId(mantenimiento.getAtraccion().getId())
            .atraccionNombre(mantenimiento.getAtraccion().getNombre())
            .visitantesAcumulados(mantenimiento.getVisitantesAcumulados())
            .tipoAlerta(mantenimiento.getTipoAlerta())
            .prioridad(mantenimiento.getPrioridad())
            .estado(mantenimiento.getEstado())
            .descripcion(mantenimiento.getDescripcion())
            .fechaGeneracion(mantenimiento.getFechaGeneracion())
            .fechaResolucion(mantenimiento.getFechaResolucion())
            .operadorId(mantenimiento.getOperador() != null ? mantenimiento.getOperador().getId() : null)
            .operadorNombre(mantenimiento.getOperador() != null ? mantenimiento.getOperador().getNombre() : null)
            .build();
    }
}