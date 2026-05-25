package com.techpark.techpark_uq.service;

import com.techpark.techpark_uq.exception.BusinessException;
import com.techpark.techpark_uq.model.dto.AlertaClimaDTO;
import com.techpark.techpark_uq.model.dto.SimulacionClimaDTO;
import com.techpark.techpark_uq.model.entity.*;
import com.techpark.techpark_uq.repository.AtraccionRepository;
import com.techpark.techpark_uq.repository.ClimaRepository;
import com.techpark.techpark_uq.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio de Simulación de Clima
 * Gestiona cierres masivos de atracciones por condiciones climáticas adversas
 * Envía notificaciones en tiempo real a los visitantes afectados
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClimaService {
    
    private final AtraccionRepository atraccionRepository;
    private final ClimaRepository climaRepository;
    private final UsuarioRepository usuarioRepository;
    private final SimpMessagingTemplate messagingTemplate;  // Para WebSocket
    
    // Tipos de atracciones afectadas por cada condición climática
    private static final Map<String, List<String>> AFECTACIONES_POR_CLIMA = Map.of(
        "TORMENTA_ELECTRICA", List.of("MECANICA", "ACUATICA"),
        "LLUVIA_FUERTE", List.of("ACUATICA", "MECANICA"),
        "VIENTO_FUERTE", List.of("MECANICA"),
        "CALOR_EXTREMO", List.of("MECANICA")
    );
    
    // Tipos de atracciones que NO se cierran (protegidas)
    private static final List<String> TIPOS_PROTEGIDOS = List.of("INFANTIL", "SHOW");
    
    // Alertas climáticas activas en memoria
    private final List<AlertaClimaDTO> alertasActivas = new ArrayList<>();
    
    // ============= MÉTODOS PRINCIPALES =============
    
    /**
     * Activar alerta climática y cerrar atracciones afectadas
     */
    @Transactional
    public AlertaClimaDTO activarAlertaClimatica(SimulacionClimaDTO simulacion, Long administradorId) {
        log.info("🌩️ ACTIVANDO ALERTA CLIMÁTICA: {} - Severidad: {}", 
                 simulacion.getTipoClima(), simulacion.getSeveridad());
        
        // Validar administrador
        Usuario administrador = usuarioRepository.findById(administradorId)
                .orElseThrow(() -> new BusinessException("Administrador no encontrado", "ADMIN_NO_ENCONTRADO"));
        
        if (administrador.getRol() != RolUsuario.ADMINISTRADOR) {
            throw new BusinessException("Solo un administrador puede activar alertas climáticas", "ROL_NO_AUTORIZADO");
        }
        
        // Desactivar alertas anteriores del mismo tipo
        List<AlertaClima> alertasPrevias = climaRepository.findByTipoAlertaAndActivaTrue(simulacion.getTipoClima());
        for (AlertaClima alerta : alertasPrevias) {
            alerta.setActiva(false);
            alerta.setFechaResolucion(LocalDateTime.now());
            climaRepository.save(alerta);
        }
        
        // Identificar atracciones afectadas
        List<Atraccion> atraccionesAfectadas = identificarAtraccionesAfectadas(
            simulacion.getTipoClima(), 
            simulacion.getSeveridad()
        );
        
        // Cerrar atracciones afectadas
        List<Long> idsAfectadas = cerrarAtraccionesPorClima(atraccionesAfectadas, simulacion);
        
        // Crear alerta
        String mensaje = construirMensajeAlerta(simulacion, atraccionesAfectadas.size());
        
        AlertaClima alerta = AlertaClima.builder()
            .tipoAlerta(simulacion.getTipoClima())
            .severidad(simulacion.getSeveridad())
            .mensaje(mensaje)
            .fechaGeneracion(LocalDateTime.now())
            .fechaFinEstimada(LocalDateTime.now().plusMinutes(simulacion.getDuracionEstimadaMinutos()))
            .activa(true)
            .atraccionesAfectadasIds(idsAfectadas.stream().map(String::valueOf).collect(Collectors.joining(",")))
            .operador(administrador)
            .build();
        
        AlertaClima saved = climaRepository.save(alerta);
        
        // Guardar en memoria
        AlertaClimaDTO dto = convertirADTO(saved);
        alertasActivas.add(dto);
        
        // Enviar notificaciones en tiempo real (WebSocket)
        enviarNotificacionesClima(dto, atraccionesAfectadas);
        
        log.warn("⚠️ Alerta climática activada. {} atracciones cerradas. Tipo: {}", 
                 atraccionesAfectadas.size(), simulacion.getTipoClima());
        
        return dto;
    }
    
    /**
     * Desactivar alerta climática y reactivar atracciones
     */
    @Transactional
    public AlertaClimaDTO desactivarAlertaClimatica(Long alertaId, Long administradorId) {
        log.info("☀️ DESACTIVANDO ALERTA CLIMÁTICA: {}", alertaId);
        
        // Validar administrador
        Usuario administrador = usuarioRepository.findById(administradorId)
                .orElseThrow(() -> new BusinessException("Administrador no encontrado", "ADMIN_NO_ENCONTRADO"));
        
        AlertaClima alerta = climaRepository.findById(alertaId)
                .orElseThrow(() -> new BusinessException("Alerta no encontrada", "ALERTA_NO_ENCONTRADA"));
        
        if (!alerta.getActiva()) {
            throw new BusinessException("La alerta ya está desactivada", "ALERTA_INACTIVA");
        }
        
        // Reactivar atracciones afectadas
        reactivarAtraccionesPorClima(alerta);
        
        // Desactivar alerta
        alerta.setActiva(false);
        alerta.setFechaResolucion(LocalDateTime.now());
        climaRepository.save(alerta);
        
        // Remover de memoria
        alertasActivas.removeIf(a -> a.getId().equals(alertaId));
        
        // Enviar notificación de fin de alerta
        enviarNotificacionFinAlerta(alerta);
        
        log.info("✅ Alerta climática desactivada. Atracciones reactivadas.");
        
        return convertirADTO(alerta);
    }
    
    /**
     * Obtener todas las alertas climáticas activas
     */
    public List<AlertaClimaDTO> obtenerAlertasActivas() {
        List<AlertaClima> alertas = climaRepository.findByActivaTrueOrderBySeveridadDescFechaGeneracionDesc();
        return alertas.stream()
            .map(this::convertirADTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Obtener historial de alertas climáticas
     */
    public List<AlertaClimaDTO> obtenerHistorialAlertas() {
        List<AlertaClima> alertas = climaRepository.findByActivaFalseOrderByFechaGeneracionDesc();
        return alertas.stream()
            .limit(50)  // Últimas 50 alertas
            .map(this::convertirADTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Obtener atracciones actualmente cerradas por clima
     */
    public List<Atraccion> obtenerAtraccionesCerradasPorClima() {
        return atraccionRepository.findAll().stream()
            .filter(a -> a.getEstado() == EstadoAtraccion.CERRADA && 
                        a.getMotivoCierre() != null &&
                        (a.getMotivoCierre().contains("climática") || 
                         a.getMotivoCierre().contains("tormenta") ||
                         a.getMotivoCierre().contains("lluvia")))
            .collect(Collectors.toList());
    }
    
    /**
     * Verificar si hay alerta climática activa
     */
    public boolean hayAlertaActiva() {
        return !alertasActivas.isEmpty();
    }
    
    /**
     * Obtener estadísticas de clima
     */
    public Map<String, Object> obtenerEstadisticasClima() {
        Map<String, Object> estadisticas = new HashMap<>();
        
        estadisticas.put("alertasActivas", alertasActivas.size());
        estadisticas.put("totalAlertasHistoricas", climaRepository.count());
        
        List<Object[]> porTipo = climaRepository.countAlertasPorTipo();
        Map<String, Long> tipoCount = new HashMap<>();
        for (Object[] row : porTipo) {
            tipoCount.put((String) row[0], (Long) row[1]);
        }
        estadisticas.put("alertasPorTipo", tipoCount);
        
        // Duración promedio de alertas
        List<AlertaClima> resueltas = climaRepository.findByActivaFalseOrderByFechaGeneracionDesc();
        if (!resueltas.isEmpty()) {
            long totalMinutos = 0;
            for (AlertaClima alerta : resueltas) {
                if (alerta.getFechaResolucion() != null) {
                    totalMinutos += java.time.Duration.between(alerta.getFechaGeneracion(), alerta.getFechaResolucion()).toMinutes();
                }
            }
            estadisticas.put("duracionPromedioMinutos", totalMinutos / resueltas.size());
        }
        
        return estadisticas;
    }
    
    // ============= MÉTODOS PRIVADOS =============
    
    /**
     * Identifica qué atracciones se ven afectadas por la condición climática
     */
    private List<Atraccion> identificarAtraccionesAfectadas(String tipoClima, String severidad) {
        List<String> tiposAfectados = AFECTACIONES_POR_CLIMA.getOrDefault(tipoClima, List.of());
        
        List<Atraccion> afectadas = atraccionRepository.findAll().stream()
            .filter(a -> a.getEstado() == EstadoAtraccion.ACTIVA)  // Solo activas
            .filter(a -> tiposAfectados.contains(a.getTipo().toString()))
            .filter(a -> !TIPOS_PROTEGIDOS.contains(a.getTipo().toString()))  // Protegidas no se cierran
            .collect(Collectors.toList());
        
        // Si severidad es ALTA, cerrar también las que están cerca (opcional)
        if ("ALTA".equals(severidad)) {
            // Cerrar también atracciones con contador alto como precaución
            afectadas.addAll(atraccionRepository.findAll().stream()
                .filter(a -> a.getEstado() == EstadoAtraccion.ACTIVA)
                .filter(a -> a.getContadorVisitantes() > 400)
                .filter(a -> !afectadas.contains(a))
                .collect(Collectors.toList()));
        }
        
        return afectadas;
    }
    
    /**
     * Cierra las atracciones afectadas por el clima
     */
    private List<Long> cerrarAtraccionesPorClima(List<Atraccion> atracciones, SimulacionClimaDTO simulacion) {
        List<Long> idsCerradas = new ArrayList<>();
        String motivo = String.format("Cerrada por %s - %s. Duración estimada: %d min", 
                                     simulacion.getTipoClima(), 
                                     simulacion.getSeveridad(),
                                     simulacion.getDuracionEstimadaMinutos());
        
        for (Atraccion atraccion : atracciones) {
            if (atraccion.getEstado() == EstadoAtraccion.ACTIVA) {
                atraccion.setEstado(EstadoAtraccion.CERRADA);
                atraccion.setMotivoCierre(motivo);
                atraccionRepository.save(atraccion);
                idsCerradas.add(atraccion.getId());
                log.info("🔒 Atracción '{}' cerrada por clima: {}", atraccion.getNombre(), simulacion.getTipoClima());
            }
        }
        
        return idsCerradas;
    }
    
    /**
     * Reactiva las atracciones que estaban cerradas por la alerta climática
     */
    private void reactivarAtraccionesPorClima(AlertaClima alerta) {
        if (alerta.getAtraccionesAfectadasIds() == null || alerta.getAtraccionesAfectadasIds().isEmpty()) {
            return;
        }
        
        String[] ids = alerta.getAtraccionesAfectadasIds().split(",");
        for (String idStr : ids) {
            try {
                Long id = Long.parseLong(idStr);
                atraccionRepository.findById(id).ifPresent(atraccion -> {
                    if (atraccion.getEstado() == EstadoAtraccion.CERRADA && 
                        atraccion.getMotivoCierre() != null &&
                        atraccion.getMotivoCierre().contains(alerta.getTipoAlerta())) {
                        
                        atraccion.setEstado(EstadoAtraccion.ACTIVA);
                        atraccion.setMotivoCierre(null);
                        atraccionRepository.save(atraccion);
                        log.info("🔓 Atracción '{}' reactivada después de alerta climática", atraccion.getNombre());
                    }
                });
            } catch (NumberFormatException e) {
                log.warn("Error al parsear ID de atracción: {}", idStr);
            }
        }
    }
    
    /**
     * Construye mensaje de alerta para notificaciones
     */
    private String construirMensajeAlerta(SimulacionClimaDTO simulacion, int numAtraccionesAfectadas) {
        String baseMensaje;
        
        switch (simulacion.getTipoClima()) {
            case "TORMENTA_ELECTRICA":
                baseMensaje = "⚡ ¡ALERTA! Tormenta eléctrica detectada. ";
                break;
            case "LLUVIA_FUERTE":
                baseMensaje = "🌧️ ¡ALERTA! Lluvia fuerte en el parque. ";
                break;
            case "VIENTO_FUERTE":
                baseMensaje = "💨 ¡ALERTA! Vientos fuertes en el área. ";
                break;
            default:
                baseMensaje = "⚠️ ALERTA CLIMÁTICA: " + simulacion.getTipoClima() + ". ";
        }
        
        String severidadMsg = simulacion.getSeveridad().equals("ALTA") ? " (Severidad ALTA)" : "";
        
        return baseMensaje + String.format("%d atracciones han sido cerradas temporalmente por seguridad.%s", 
                                          numAtraccionesAfectadas, severidadMsg);
    }
    
    /**
     * Envía notificaciones en tiempo real a los visitantes afectados (WebSocket)
     */
    private void enviarNotificacionesClima(AlertaClimaDTO alerta, List<Atraccion> atraccionesAfectadas) {
        Map<String, Object> notificacion = new HashMap<>();
        notificacion.put("tipo", "ALERTA_CLIMA");
        notificacion.put("alerta", alerta);
        notificacion.put("atraccionesAfectadas", atraccionesAfectadas.stream()
            .map(a -> Map.of("id", a.getId(), "nombre", a.getNombre()))
            .collect(Collectors.toList()));
        notificacion.put("timestamp", LocalDateTime.now());
        
        // Enviar a todos los clientes conectados via WebSocket
        try {
            messagingTemplate.convertAndSend("/topic/clima", notificacion);
            log.info("📡 Notificación climática enviada a todos los clientes");
        } catch (Exception e) {
            log.warn("Error enviando notificación WebSocket: {}", e.getMessage());
        }
        
        // También podríamos enviar notificaciones push a dispositivos móviles aquí
    }
    
    /**
     * Envía notificación de fin de alerta
     */
    private void enviarNotificacionFinAlerta(AlertaClima alerta) {
        Map<String, Object> notificacion = new HashMap<>();
        notificacion.put("tipo", "FIN_ALERTA_CLIMA");
        notificacion.put("alertaId", alerta.getId());
        notificacion.put("tipoAlerta", alerta.getTipoAlerta());
        notificacion.put("mensaje", "✅ La alerta climática ha terminado. Las atracciones están reactivándose.");
        notificacion.put("timestamp", LocalDateTime.now());
        
        try {
            messagingTemplate.convertAndSend("/topic/clima", notificacion);
            log.info("📡 Notificación de fin de alerta enviada");
        } catch (Exception e) {
            log.warn("Error enviando notificación WebSocket: {}", e.getMessage());
        }
    }
    
    /**
     * Convierte entidad a DTO
     */
    private AlertaClimaDTO convertirADTO(AlertaClima alerta) {
        List<Long> idsAfectadas = new ArrayList<>();
        List<String> nombresAfectados = new ArrayList<>();
        
        if (alerta.getAtraccionesAfectadasIds() != null && !alerta.getAtraccionesAfectadasIds().isEmpty()) {
            String[] ids = alerta.getAtraccionesAfectadasIds().split(",");
            for (String idStr : ids) {
                try {
                    Long id = Long.parseLong(idStr);
                    idsAfectadas.add(id);
                    
                    atraccionRepository.findById(id).ifPresent(a -> 
                        nombresAfectados.add(a.getNombre())
                    );
                } catch (NumberFormatException e) {
                    log.warn("Error al parsear ID: {}", idStr);
                }
            }
        }
        
        return AlertaClimaDTO.builder()
            .id(alerta.getId())
            .tipoAlerta(alerta.getTipoAlerta())
            .severidad(alerta.getSeveridad())
            .mensaje(alerta.getMensaje())
            .fechaGeneracion(alerta.getFechaGeneracion())
            .fechaFinEstimada(alerta.getFechaFinEstimada())
            .activa(alerta.getActiva())
            .atraccionesAfectadasIds(idsAfectadas)
            .atraccionesAfectadasNombres(nombresAfectados)
            .operadorId(alerta.getOperador() != null ? alerta.getOperador().getId() : null)
            .operadorNombre(alerta.getOperador() != null ? alerta.getOperador().getNombre() : null)
            .build();
    }
}