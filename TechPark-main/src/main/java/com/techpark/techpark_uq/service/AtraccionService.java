package com.techpark.techpark_uq.service;

import com.techpark.techpark_uq.exception.BusinessException;
import com.techpark.techpark_uq.model.dto.AtraccionDTO;
import com.techpark.techpark_uq.model.entity.*;
import com.techpark.techpark_uq.repository.AtraccionRepository;
import com.techpark.techpark_uq.repository.VisitanteRepository;
import com.techpark.techpark_uq.repository.ZonaRepository;
import com.techpark.techpark_uq.mapper.AtraccionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AtraccionService {
    
    private final AtraccionRepository atraccionRepository;
    private final ZonaRepository zonaRepository;
    private final AtraccionMapper atraccionMapper;
    private final VisitanteRepository visitanteRepository;
    private final MantenimientoService mantenimientoService;  // Inyectado para verificar mantenimiento
    
    // Constantes
    private static final int LIMITE_VISITANTES_MANTENIMIENTO = 500;
    private static final int TIEMPO_ESPERA_BASE = 5;  // minutos base de espera
    
    // ============= MÉTODOS CRUD BÁSICOS =============
    
    /**
     * Obtener todas las atracciones
     */
    public List<AtraccionDTO> listarTodas() {
        log.info("Listando todas las atracciones");
        return atraccionRepository.findAll().stream()
                .map(atraccionMapper::toDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtener atracción por ID
     */
    public AtraccionDTO obtenerPorId(Long id) {
        log.info("Buscando atracción con id: {}", id);
        Atraccion atraccion = atraccionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Atracción no encontrada", "ATRACCION_NO_ENCONTRADA"));
        return atraccionMapper.toDto(atraccion);
    }
    
    /**
     * Obtener entidad Atraccion por ID (para uso interno)
     */
    public Atraccion obtenerEntidadPorId(Long id) {
        return atraccionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Atracción no encontrada", "ATRACCION_NO_ENCONTRADA"));
    }
    
    /**
     * Crear nueva atracción
     */
    @Transactional
    public AtraccionDTO crear(AtraccionDTO atraccionDTO) {
        log.info("Creando nueva atracción: {}", atraccionDTO.getNombre());
        
        // Validar que la zona existe
        Zona zona = zonaRepository.findById(atraccionDTO.getZonaId())
                .orElseThrow(() -> new BusinessException("Zona no encontrada", "ZONA_NO_ENCONTRADA"));
        
        // Validar que no exista una atracción con el mismo nombre
        if (atraccionRepository.findAll().stream().anyMatch(a -> a.getNombre().equalsIgnoreCase(atraccionDTO.getNombre()))) {
            throw new BusinessException("Ya existe una atracción con ese nombre", "NOMBRE_DUPLICADO");
        }
        
        Atraccion atraccion = atraccionMapper.toEntity(atraccionDTO);
        atraccion.setZona(zona);
        atraccion.setEstado(EstadoAtraccion.ACTIVA);
        atraccion.setContadorVisitantes(0);
        
        // Calcular tiempo de espera estimado inicial
        atraccion.setTiempoEsperaEstimado(calcularTiempoEspera(atraccion));
        
        Atraccion saved = atraccionRepository.save(atraccion);
        log.info("Atracción creada exitosamente: {} en zona {}", saved.getNombre(), zona.getNombre());
        
        return atraccionMapper.toDto(saved);
    }
    
    /**
     * Actualizar atracción existente
     */
    @Transactional
    public AtraccionDTO actualizar(Long id, AtraccionDTO atraccionDTO) {
        log.info("Actualizando atracción con id: {}", id);
        
        Atraccion atraccion = atraccionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Atracción no encontrada", "ATRACCION_NO_ENCONTRADA"));
        
        // Actualizar zona si cambió
        if (atraccionDTO.getZonaId() != null && !atraccionDTO.getZonaId().equals(atraccion.getZona().getId())) {
            Zona nuevaZona = zonaRepository.findById(atraccionDTO.getZonaId())
                    .orElseThrow(() -> new BusinessException("Zona no encontrada", "ZONA_NO_ENCONTRADA"));
            atraccion.setZona(nuevaZona);
        }
        
        // Actualizar campos
        if (atraccionDTO.getNombre() != null) {
            atraccion.setNombre(atraccionDTO.getNombre());
        }
        if (atraccionDTO.getTipo() != null) {
            atraccion.setTipo(TipoAtraccion.valueOf(atraccionDTO.getTipo()));
        }
        if (atraccionDTO.getCapacidadMaxima() != null) {
            atraccion.setCapacidadMaxima(atraccionDTO.getCapacidadMaxima());
        }
        if (atraccionDTO.getAlturaMinima() != null) {
            atraccion.setAlturaMinima(atraccionDTO.getAlturaMinima());
        }
        if (atraccionDTO.getEdadMinima() != null) {
            atraccion.setEdadMinima(atraccionDTO.getEdadMinima());
        }
        if (atraccionDTO.getCostoAdicional() != null) {
            atraccion.setCostoAdicional(atraccionDTO.getCostoAdicional());
        }
        if (atraccionDTO.getPosicionX() != null) {
            atraccion.setPosicionX(atraccionDTO.getPosicionX());
        }
        if (atraccionDTO.getPosicionY() != null) {
            atraccion.setPosicionY(atraccionDTO.getPosicionY());
        }
        
        // Recalcular tiempo de espera
        atraccion.setTiempoEsperaEstimado(calcularTiempoEspera(atraccion));
        
        Atraccion updated = atraccionRepository.save(atraccion);
        log.info("Atracción actualizada: {}", updated.getNombre());
        
        return atraccionMapper.toDto(updated);
    }
    
    /**
     * Eliminar atracción (soft delete - cambiar estado a CERRADA)
     */
    @Transactional
    public void eliminar(Long id) {
        log.info("Eliminando (cerrando) atracción con id: {}", id);
        
        Atraccion atraccion = atraccionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Atracción no encontrada", "ATRACCION_NO_ENCONTRADA"));
        
        atraccion.setEstado(EstadoAtraccion.CERRADA);
        atraccion.setMotivoCierre("Eliminada por administrador");
        atraccionRepository.save(atraccion);
        
        log.info("Atracción {} marcada como CERRADA", atraccion.getNombre());
    }
    
    // ============= MÉTODOS DE ESTADO =============
    
    /**
     * Actualizar estado de atracción
     */
    @Transactional
    public AtraccionDTO actualizarEstado(Long id, String nuevoEstado, String motivo) {
        log.info("Actualizando estado de atracción {} a {}", id, nuevoEstado);
        
        Atraccion atraccion = atraccionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Atracción no encontrada", "ATRACCION_NO_ENCONTRADA"));
        
        EstadoAtraccion estado = EstadoAtraccion.valueOf(nuevoEstado);
        atraccion.setEstado(estado);
        
        if (motivo != null && !motivo.isEmpty()) {
            atraccion.setMotivoCierre(motivo);
        } else if (estado == EstadoAtraccion.CERRADA) {
            atraccion.setMotivoCierre("Cerrada por administrador");
        } else if (estado == EstadoAtraccion.MANTENIMIENTO) {
            atraccion.setMotivoCierre("En mantenimiento programado");
        } else if (estado == EstadoAtraccion.ACTIVA) {
            atraccion.setMotivoCierre(null);
        }
        
        Atraccion updated = atraccionRepository.save(atraccion);
        log.info("Atracción {} ahora está en estado {}", updated.getNombre(), updated.getEstado());
        
        return atraccionMapper.toDto(updated);
    }
    
    // ============= MÉTODOS DE CONSULTA =============
    
    /**
     * Obtener atracciones por estado
     */
    public List<AtraccionDTO> listarPorEstado(String estado) {
        log.info("Listando atracciones con estado: {}", estado);
        EstadoAtraccion estadoEnum = EstadoAtraccion.valueOf(estado);
        return atraccionRepository.findByEstado(estadoEnum).stream()
                .map(atraccionMapper::toDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtener atracciones por tipo
     */
    public List<AtraccionDTO> listarPorTipo(String tipo) {
        log.info("Listando atracciones de tipo: {}", tipo);
        TipoAtraccion tipoEnum = TipoAtraccion.valueOf(tipo);
        return atraccionRepository.findByTipo(tipoEnum).stream()
                .map(atraccionMapper::toDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtener atracciones por zona
     */
    public List<AtraccionDTO> listarPorZona(Long zonaId) {
        log.info("Listando atracciones de zona: {}", zonaId);
        return atraccionRepository.findByZonaId(zonaId).stream()
                .map(atraccionMapper::toDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtener atracciones activas
     */
    public List<AtraccionDTO> listarActivas() {
        log.info("Listando atracciones activas");
        return atraccionRepository.findByEstado(EstadoAtraccion.ACTIVA).stream()
                .map(atraccionMapper::toDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtener atracciones que necesitan mantenimiento (contador >= 500)
     */
    public List<AtraccionDTO> obtenerNecesitanMantenimiento() {
        log.info("Listando atracciones que necesitan mantenimiento");
        return atraccionRepository.findAtraccionesQueNecesitanMantenimiento().stream()
                .map(atraccionMapper::toDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtener atracciones por rango de contador
     */
    public List<AtraccionDTO> obtenerPorRangoContador(int min, int max) {
        log.info("Listando atracciones con contador entre {} y {}", min, max);
        return atraccionRepository.findAll().stream()
                .filter(a -> a.getContadorVisitantes() >= min && a.getContadorVisitantes() <= max)
                .map(atraccionMapper::toDto)
                .collect(Collectors.toList());
    }
    
    // ============= MÉTODO CRÍTICO: INCREMENTAR CONTADOR =============
    
    /**
     * Incrementar contador de visitantes de una atracción
     * IMPORTANTE: Este método activa la verificación de mantenimiento automática
     */
    @Transactional
    public void incrementarContador(Long atraccionId) {
        log.info("Incrementando contador de visitantes para atracción {}", atraccionId);
        
        // Verificar que la atracción existe
        Atraccion atraccion = atraccionRepository.findById(atraccionId)
                .orElseThrow(() -> new BusinessException("Atracción no encontrada", "ATRACCION_NO_ENCONTRADA"));
        
        // Verificar que la atracción está activa
        if (atraccion.getEstado() != EstadoAtraccion.ACTIVA) {
            throw new BusinessException(
                String.format("No se puede acceder a la atracción '%s'. Estado actual: %s", 
                             atraccion.getNombre(), atraccion.getEstado()),
                "ATRACCION_NO_DISPONIBLE");
        }
        
        // Incrementar contador
        atraccionRepository.incrementarContadorVisitantes(atraccionId);
        
        // Obtener la atracción actualizada
        Atraccion atraccionActualizada = atraccionRepository.findById(atraccionId).get();
        int visitantesActuales = atraccionActualizada.getContadorVisitantes();
        
        log.info("Atracción {} ahora tiene {} visitantes acumulados", 
                 atraccionActualizada.getNombre(), visitantesActuales);
        
        // Actualizar tiempo de espera estimado basado en la afluencia
        actualizarTiempoEsperaEstimado(atraccionActualizada);
        
        // ⚠️ CRUCIAL: Verificar si necesita mantenimiento (regla de 500 visitantes)
        mantenimientoService.verificarMantenimiento(atraccionId);
        
        // Log de advertencia si está cerca del límite
        if (visitantesActuales >= 450 && visitantesActuales < 500) {
            log.warn("⚠️ Atracción {} está cerca del límite de mantenimiento: {}/{} visitantes", 
                     atraccionActualizada.getNombre(), visitantesActuales, LIMITE_VISITANTES_MANTENIMIENTO);
        }
    }
    
    /**
     * Resetear contador de visitantes (después de mantenimiento)
     */
    @Transactional
    public void resetearContador(Long atraccionId) {
        log.info("Reseteando contador de visitantes para atracción {}", atraccionId);
        
        Atraccion atraccion = atraccionRepository.findById(atraccionId)
                .orElseThrow(() -> new BusinessException("Atracción no encontrada", "ATRACCION_NO_ENCONTRADA"));
        
        atraccion.setContadorVisitantes(0);
        atraccion.setTiempoEsperaEstimado(calcularTiempoEspera(atraccion));
        atraccionRepository.save(atraccion);
        
        log.info("Contador de {} reseteado a 0", atraccion.getNombre());
    }
    
    // ============= MÉTODOS DE TIEMPO DE ESPERA =============
    
    /**
     * Actualizar tiempo de espera estimado basado en contador actual
     */
    private void actualizarTiempoEsperaEstimado(Atraccion atraccion) {
        int nuevoTiempo = calcularTiempoEspera(atraccion);
        atraccion.setTiempoEsperaEstimado(nuevoTiempo);
        atraccionRepository.save(atraccion);
    }
    
    /**
     * Calcular tiempo de espera basado en contador y capacidad
     */
    private int calcularTiempoEspera(Atraccion atraccion) {
        int visitantes = atraccion.getContadorVisitantes();
        int capacidad = atraccion.getCapacidadMaxima() != null ? atraccion.getCapacidadMaxima() : 20;
        
        if (visitantes < capacidad) {
            return TIEMPO_ESPERA_BASE;
        }
        
        int ciclos = (int) Math.ceil((double) visitantes / capacidad);
        return TIEMPO_ESPERA_BASE * ciclos;
    }
    
    /**
     * Calcular tiempo de espera estimado para mostrar al visitante
     */
    public int calcularTiempoEsperaEstimado(Long atraccionId) {
        Atraccion atraccion = obtenerEntidadPorId(atraccionId);
        return calcularTiempoEspera(atraccion);
    }
    
    // ============= MÉTODOS DE VALIDACIÓN =============
    
    /**
     * Validar si un visitante puede acceder a una atracción
     */
    public boolean puedeAcceder(Long atraccionId, int edad, double estatura, String tipoTicket) {
        Atraccion atraccion = obtenerEntidadPorId(atraccionId);
        
        // Validar que la atracción esté activa
        if (atraccion.getEstado() != EstadoAtraccion.ACTIVA) {
            log.warn("Atracción {} no está activa. Estado: {}", atraccion.getNombre(), atraccion.getEstado());
            return false;
        }
        
        // Validar edad
        if (edad < atraccion.getEdadMinima()) {
            log.warn("Visitante no cumple edad mínima para {}. Requiere: {}", atraccion.getNombre(), atraccion.getEdadMinima());
            return false;
        }
        
        // Validar estatura
        if (estatura < atraccion.getAlturaMinima()) {
            log.warn("Visitante no cumple estatura mínima para {}. Requiere: {}", atraccion.getNombre(), atraccion.getAlturaMinima());
            return false;
        }
        
        // Para tickets General, validar saldo si hay costo adicional
        if ("GENERAL".equalsIgnoreCase(tipoTicket) && 
            atraccion.getCostoAdicional() != null && 
            atraccion.getCostoAdicional() > 0) {
            // La validación de saldo se hace en ColaVirtualService
            log.info("Atracción {} tiene costo adicional de ${}", atraccion.getNombre(), atraccion.getCostoAdicional());
        }
        
        return true;
    }
    
    // ============= MÉTODOS DE REPORTE =============
    
    /**
     * Obtener estadísticas de todas las atracciones
     */
    public List<Object[]> obtenerEstadisticas() {
        log.info("Generando estadísticas de atracciones");
        return atraccionRepository.findAll().stream()
            .map(a -> new Object[]{
                a.getId(),
                a.getNombre(),
                a.getContadorVisitantes(),
                a.getTiempoEsperaEstimado(),
                a.getEstado(),
                a.getZona().getNombre()
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Obtener atracciones más visitadas
     */
    public List<AtraccionDTO> obtenerMasVisitadas(int limite) {
        log.info("Obteniendo top {} atracciones más visitadas", limite);
        return atraccionRepository.findAll().stream()
                .sorted((a1, a2) -> a2.getContadorVisitantes().compareTo(a1.getContadorVisitantes()))
                .limit(limite)
                .map(atraccionMapper::toDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtener tiempo promedio de espera general del parque
     */
    public double obtenerTiempoPromedioGeneral() {
        Double promedio = atraccionRepository.calcularTiempoEsperaPromedio();
        return promedio != null ? promedio : 0.0;
    }
    
    /**
     * Obtener porcentaje de ocupación de una atracción
     */
    public double obtenerPorcentajeOcupacion(Long atraccionId) {
        Atraccion atraccion = obtenerEntidadPorId(atraccionId);
        int capacidad = atraccion.getCapacidadMaxima() != null ? atraccion.getCapacidadMaxima() : 20;
        int enCola = atraccion.getContadorVisitantes();
        
        return Math.min(100.0, (double) enCola / capacidad * 100);
    }

    // ============= MÉTODOS DE FAVORITOS =============

    // ============= MÉTODOS DE FAVORITOS =============

    /**
     * Obtener favoritos del visitante
     */
    public List<AtraccionDTO> obtenerFavoritos(Long visitanteId) {
        System.out.println("⭐ Obteniendo favoritos del visitante: " + visitanteId);

        Visitante visitante = visitanteRepository.findById(visitanteId)
                .orElseThrow(() -> new BusinessException("Visitante no encontrado", "VISITANTE_NO_ENCONTRADO"));

        return visitante.getAtraccionesFavoritas().stream()
                .map(this::obtenerPorId)
                .collect(Collectors.toList());
    }

    /**
     * Agregar a favoritos
     */
    @Transactional
    public void agregarFavorito(Long visitanteId, Long atraccionId) {
        System.out.println("⭐ Agregando atracción " + atraccionId + " a favoritos del visitante " + visitanteId);

        // Validar que la atracción existe
        obtenerEntidadPorId(atraccionId);

        // Obtener visitante
        Visitante visitante = visitanteRepository.findById(visitanteId)
                .orElseThrow(() -> new BusinessException("Visitante no encontrado", "VISITANTE_NO_ENCONTRADO"));

        // Agregar a favoritos
        visitante.getAtraccionesFavoritas().add(atraccionId);
        visitanteRepository.save(visitante);

        System.out.println("✅ Atracción agregada a favoritos");
    }

    /**
     * Remover de favoritos
     */
    @Transactional
    public void removerFavorito(Long visitanteId, Long atraccionId) {
        System.out.println("❌ Removiendo atracción " + atraccionId + " de favoritos del visitante " + visitanteId);

        // Validar que la atracción existe
        obtenerEntidadPorId(atraccionId);

        // Obtener visitante
        Visitante visitante = visitanteRepository.findById(visitanteId)
                .orElseThrow(() -> new BusinessException("Visitante no encontrado", "VISITANTE_NO_ENCONTRADO"));

        // Remover de favoritos
        visitante.getAtraccionesFavoritas().remove(atraccionId);
        visitanteRepository.save(visitante);

        System.out.println("✅ Atracción removida de favoritos");
    }
}