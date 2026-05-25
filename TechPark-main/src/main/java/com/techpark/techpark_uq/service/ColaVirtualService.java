package com.techpark.techpark_uq.service;

import com.techpark.techpark_uq.estructuras.ColaPrioridad;
import com.techpark.techpark_uq.exception.BusinessException;
import com.techpark.techpark_uq.model.dto.RespuestaColaDTO;
import com.techpark.techpark_uq.model.dto.SolicitudColaDTO;
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
 * Servicio para gestionar colas virtuales con prioridad (Fast-Pass > General)
 * Utiliza nuestra implementación propia de ColaPrioridad
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ColaVirtualService {

    private final VisitanteRepository visitanteRepository;
    private final AtraccionRepository atraccionRepository;
    private final ColaVirtualRepository colaVirtualRepository;
    private final AtraccionService atraccionService;
    private final HistorialVisitaRepository historialVisitaRepository;

    // Mapa en memoria: AtraccionId -> ColaPrioridad (para acceso rápido)
    private final Map<Long, ColaPrioridad<ElementoCola>> colasEnMemoria = new ConcurrentHashMap<>();

    // Mapa para almacenar la posición de cada visitante en cada atracción
    private final Map<String, InformacionCola> informacionCola = new ConcurrentHashMap<>();

    // Tiempo base estimado por persona en minutos
    private static final int TIEMPO_BASE_POR_PERSONA = 3;
    private static final int CAPACIDAD_CICLO_BASE = 20;

    // ============= CLASES INTERNAS =============

    /**
     * Elemento que se guarda en nuestra ColaPrioridad
     */
    @lombok.Getter
    @lombok.AllArgsConstructor
    public static class ElementoCola {
        private final Long visitanteId;
        private final String nombreVisitante;
        private final String tipoTicket; // FAST_PASS, GENERAL, FAMILIAR
        private final LocalDateTime horaIngreso;
        private final Long atraccionId;

        public int obtenerPrioridad() {
            if ("FAST_PASS".equalsIgnoreCase(tipoTicket)) {
                return 1; // Mayor prioridad
            } else if ("FAMILIAR".equalsIgnoreCase(tipoTicket)) {
                return 2; // Prioridad media
            }
            return 3; // GENERAL - menor prioridad
        }

        @Override
        public String toString() {
            return String.format("%s (%s) - prioridad: %d", nombreVisitante, tipoTicket, obtenerPrioridad());
        }
    }

    /**
     * Información adicional de la cola para cálculos
     */
    @lombok.Getter
    @lombok.AllArgsConstructor
    public static class InformacionCola {
        private Integer posicion;
        private LocalDateTime horaIngreso;
        private Integer prioridad;
        private Boolean atendido;
    }

    // ============= MÉTODOS PRINCIPALES =============

    /**
     * Unirse a la cola virtual de una atracción
     */
    @Transactional
    public RespuestaColaDTO unirseACola(SolicitudColaDTO solicitud) {
        log.info("Visitante {} solicitando unirse a cola de atracción {}",
                solicitud.getVisitanteId(), solicitud.getAtraccionId());

        // Validar visitante
        Visitante visitante = visitanteRepository.findById(solicitud.getVisitanteId())
                .orElseThrow(() -> new BusinessException("Visitante no encontrado", "VISITANTE_NO_ENCONTRADO"));

        // Validar atracción
        Atraccion atraccion = atraccionRepository.findById(solicitud.getAtraccionId())
                .orElseThrow(() -> new BusinessException("Atracción no encontrada", "ATRACCION_NO_ENCONTRADA"));

        // Validar que la atracción esté activa
        if (atraccion.getEstado() != EstadoAtraccion.ACTIVA) {
            throw new BusinessException(
                    String.format("La atracción '%s' no está disponible. Estado: %s",
                            atraccion.getNombre(), atraccion.getEstado()),
                    "ATRACCION_NO_DISPONIBLE");
        }

        // Validar restricciones del visitante para esta atracción
        validarRestriccionesVisitante(visitante, atraccion);

        // Verificar si el visitante ya está en la cola
        String key = generarKey(visitante.getId(), atraccion.getId());
        InformacionCola infoExistente = informacionCola.get(key);
        if (infoExistente != null && !infoExistente.getAtendido()) {
            throw new BusinessException("El visitante ya está en la cola de esta atracción", "YA_EN_COLA");
        }

        // Obtener o crear la cola para esta atracción
        ColaPrioridad<ElementoCola> cola = colasEnMemoria.computeIfAbsent(
                atraccion.getId(),
                k -> new ColaPrioridad<>());

        // Crear elemento para la cola
        ElementoCola elemento = new ElementoCola(
                visitante.getId(),
                visitante.getNombre(),
                visitante.getTicketActivo(),
                LocalDateTime.now(),
                atraccion.getId());

        // Encolar con su prioridad correspondiente
        cola.encolar(elemento, elemento.obtenerPrioridad());

        // Guardar en base de datos
        ColaVirtual colaVirtual = ColaVirtual.builder()
                .visitante(visitante)
                .atraccion(atraccion)
                .prioridad(elemento.obtenerPrioridad())
                .horaIngresoCola(LocalDateTime.now())
                .atendido(false)
                .posicion(cola.getTamanio())
                .build();

        colaVirtualRepository.save(colaVirtual);

        // Guardar información en memoria
        informacionCola.put(key, new InformacionCola(
                cola.getTamanio(),
                LocalDateTime.now(),
                elemento.obtenerPrioridad(),
                false));

        // Calcular respuesta
        int posicion = calcularPosicionReal(cola, visitante.getId());
        int tiempoEstimado = calcularTiempoEstimadoEspera(cola, atraccion, posicion);
        int personasDelante = contarPersonasDelante(cola, visitante.getId());

        log.info("Visitante {} unido a cola de {}. Posición: {}, Tiempo estimado: {} min",
                visitante.getNombre(), atraccion.getNombre(), posicion, tiempoEstimado);

        return RespuestaColaDTO.builder()
                .posicion(posicion)
                .tiempoEstimadoEspera(tiempoEstimado)
                .personasDelante(personasDelante)
                .tienePrioridad("FAST_PASS".equalsIgnoreCase(visitante.getTicketActivo()))
                .mensaje(construirMensajeCola(posicion, tiempoEstimado, visitante.getTicketActivo()))
                .build();
    }

    /**
     * Procesar al siguiente visitante en la cola (para el operador)
     * Este método usa la prioridad para dar paso primero a Fast-Pass
     */
    @Transactional
    public ElementoCola siguienteEnCola(Long atraccionId, Long operadorId) {
        log.info("🔍 Operador {} solicitando siguiente en cola de atracción {}", operadorId, atraccionId);

        // Validar que la atracción existe
        Atraccion atraccion = atraccionRepository.findById(atraccionId)
                .orElseThrow(() -> new BusinessException("Atracción no encontrada", "ATRACCION_NO_ENCONTRADA"));

        log.info("Atracción encontrada: {} - Estado: {}", atraccion.getNombre(), atraccion.getEstado());

        // Validar que la atracción esté activa
        if (atraccion.getEstado() != EstadoAtraccion.ACTIVA) {
            throw new BusinessException(
                    String.format("La atracción '%s' no está disponible para atender visitantes. Estado: %s",
                            atraccion.getNombre(), atraccion.getEstado()),
                    "ATRACCION_NO_DISPONIBLE");
        }

        // Buscar en la base de datos primero
        List<ColaVirtual> colasDB = colaVirtualRepository
                .findByAtraccionIdAndAtendidoFalseOrderByPrioridadAscHoraIngresoColaAsc(atraccionId);

        log.info("📊 Colas en BD para atracción {}: {}", atraccionId, colasDB.size());

        for (ColaVirtual cv : colasDB) {
            log.info("  - Visitante ID: {}, Prioridad: {}, Ingreso: {}",
                    cv.getVisitante().getId(), cv.getPrioridad(), cv.getHoraIngresoCola());
        }

        if (colasDB.isEmpty()) {
            log.warn("⚠️ No hay visitantes en cola para atracción {}", atraccionId);
            throw new BusinessException("No hay visitantes en la cola", "COLA_VACIA");
        }

        ColaVirtual primeraCola = colasDB.get(0);
        log.info("✅ Visitante seleccionado: {} (Prioridad: {})",
                primeraCola.getVisitante().getNombre(), primeraCola.getPrioridad());

        // Crear elemento para retornar
        Visitante visitante = primeraCola.getVisitante();
        ElementoCola siguiente = new ElementoCola(
                visitante.getId(),
                visitante.getNombre(),
                visitante.getTicketActivo(),
                primeraCola.getHoraIngresoCola(),
                atraccionId);

        // Marcar como atendido
        primeraCola.setAtendido(true);
        primeraCola.setFechaAtencion(LocalDateTime.now());
        if (primeraCola.getHoraIngresoCola() != null) {
            long minutosEspera = ChronoUnit.MINUTES.between(primeraCola.getHoraIngresoCola(), LocalDateTime.now());
            primeraCola.setTiempoEsperaReal((int) minutosEspera);
            log.info("⏱️ Tiempo de espera del visitante: {} minutos", minutosEspera);
        }
        colaVirtualRepository.save(primeraCola);

        // ✅ Registrar en historial de visitas (cuando ya fue atendido)
        HistorialVisita historial = new HistorialVisita();
        historial.setVisitante(visitante);
        historial.setAtraccion(atraccion);
        historial.setFechaVisita(primeraCola.getFechaAtencion() != null
                ? primeraCola.getFechaAtencion()
                : LocalDateTime.now());
        historial.setTiempoEsperaReal(primeraCola.getTiempoEsperaReal());

// usoFastPass: true si fue prioridad alta (1) o ticket FAST_PASS
        boolean usoFastPass = (primeraCola.getPrioridad() != null && primeraCola.getPrioridad() == 1)
                || ("FAST_PASS".equalsIgnoreCase(visitante.getTicketActivo()));
        historial.setUsoFastPass(usoFastPass);

        historialVisitaRepository.save(historial);

        // Incrementar contador de visitantes de la atracción
        atraccionService.incrementarContador(atraccionId);

        log.info("✅ Visitante {} atendido correctamente en {}",
                siguiente.getNombreVisitante(), atraccion.getNombre());

        return siguiente;
    }

    /**
     * Cancelar la posición en cola de un visitante
     */
    /**
     * Cancelar la posición en cola de un visitante
     */
    @Transactional
    public boolean cancelarCola(Long visitanteId, Long atraccionId) {
        log.info("🗑️ Cancelando cola de visitante {} en atracción {}", visitanteId, atraccionId);

        // Buscar la cola activa usando el repository directamente
        List<ColaVirtual> colasActivas = colaVirtualRepository.findByVisitanteIdAndAtendidoFalse(visitanteId);
        log.info("Colas activas encontradas para visitante {}: {}", visitanteId, colasActivas.size());

        // Filtrar por atracción
        ColaVirtual cola = null;
        for (ColaVirtual cv : colasActivas) {
            if (cv.getAtraccion().getId().equals(atraccionId)) {
                cola = cv;
                break;
            }
        }

        if (cola == null) {
            log.warn("No se encontró cola activa para visitante {} en atracción {}", visitanteId, atraccionId);
            return false;
        }

        log.info("✅ Cola encontrada - ID: {}, Atraccion: {}", cola.getId(), cola.getAtraccion().getNombre());

        // Marcar como cancelada/atendida
        cola.setAtendido(true);
        cola.setFechaAtencion(LocalDateTime.now());
        colaVirtualRepository.save(cola);

        // Eliminar de la cola en memoria
        ColaPrioridad<ElementoCola> colaMemoria = colasEnMemoria.get(atraccionId);
        if (colaMemoria != null && !colaMemoria.estaVacia()) {
            ColaPrioridad<ElementoCola> nuevaCola = new ColaPrioridad<>();
            List<ElementoCola> elementos = new ArrayList<>();

            while (!colaMemoria.estaVacia()) {
                ElementoCola elem = colaMemoria.desencolar();
                if (!elem.getVisitanteId().equals(visitanteId)) {
                    elementos.add(elem);
                }
            }

            for (ElementoCola elem : elementos) {
                nuevaCola.encolar(elem, elem.obtenerPrioridad());
            }
            colasEnMemoria.put(atraccionId, nuevaCola);
        }

        // Eliminar de información en memoria
        String key = generarKey(visitanteId, atraccionId);
        informacionCola.remove(key);

        log.info("✅ Cola cancelada exitosamente");

        return true;
    }

    // ============= MÉTODOS DE CONSULTA =============

    /**
     * Obtener el estado actual de la cola de una atracción
     */
    public Map<String, Object> obtenerEstadoCola(Long atraccionId) {
        Map<String, Object> estado = new HashMap<>();

        ColaPrioridad<ElementoCola> cola = colasEnMemoria.get(atraccionId);
        int totalEnCola = cola != null ? cola.getTamanio() : 0;

        estado.put("atraccionId", atraccionId);
        estado.put("totalEnCola", totalEnCola);
        estado.put("tiempoEstimadoPromedio", calcularTiempoPromedioPorAtraccion(atraccionId));

        return estado;
    }

    /**
     * Obtener la posición actual de un visitante en la cola
     */
    public Integer obtenerPosicionVisitante(Long visitanteId, Long atraccionId) {
        ColaPrioridad<ElementoCola> cola = colasEnMemoria.get(atraccionId);
        if (cola == null) {
            return null;
        }
        return calcularPosicionReal(cola, visitanteId);
    }

    /**
     * Obtener todas las colas activas del sistema
     */
    public Map<Long, Map<String, Object>> obtenerTodasLasColas() {
        Map<Long, Map<String, Object>> todasLasColas = new HashMap<>();

        for (Map.Entry<Long, ColaPrioridad<ElementoCola>> entry : colasEnMemoria.entrySet()) {
            todasLasColas.put(entry.getKey(), obtenerEstadoCola(entry.getKey()));
        }

        return todasLasColas;
    }

    // ============= MÉTODOS PRIVADOS =============

    /**
     * Valida que el visitante cumpla con las restricciones de la atracción
     */
    private void validarRestriccionesVisitante(Visitante visitante, Atraccion atraccion) {
        // Validar edad
        if (visitante.getEdad() < atraccion.getEdadMinima()) {
            throw new BusinessException(
                    String.format("Edad mínima requerida: %d años. Tu edad: %d",
                            atraccion.getEdadMinima(), visitante.getEdad()),
                    "EDAD_NO_CUMPLE");
        }

        // Validar estatura
        if (visitante.getEstatura() < atraccion.getAlturaMinima()) {
            throw new BusinessException(
                    String.format("Altura mínima requerida: %.2f m. Tu altura: %.2f m",
                            atraccion.getAlturaMinima(), visitante.getEstatura()),
                    "ALTURA_NO_CUMPLE");
        }

        // Verificar costo adicional para tickets General
        if ("GENERAL".equalsIgnoreCase(visitante.getTicketActivo()) &&
                atraccion.getCostoAdicional() != null &&
                atraccion.getCostoAdicional() > 0) {

            Double saldoActual = visitante.getSaldoVirtual() != null ? visitante.getSaldoVirtual() : 0.0;
            if (saldoActual < atraccion.getCostoAdicional()) {
                throw new BusinessException(
                        String.format("Saldo insuficiente. Costo adicional: $%.2f, Saldo: $%.2f",
                                atraccion.getCostoAdicional(), saldoActual),
                        "SALDO_INSUFICIENTE");
            }

            // Descontar saldo
            visitante.setSaldoVirtual(saldoActual - atraccion.getCostoAdicional());
            visitanteRepository.save(visitante);
        }
    }

    /**
     * Calcula la posición real considerando prioridades
     */
    private int calcularPosicionReal(ColaPrioridad<ElementoCola> cola, Long visitanteId) {
        int posicion = 1;

        List<ElementoCola> elementos = new ArrayList<>();
        while (!cola.estaVacia()) {
            ElementoCola elem = cola.desencolar();
            if (!elem.getVisitanteId().equals(visitanteId)) {
                posicion++;
            } else {
                elementos.add(elem);
                break;
            }
            elementos.add(elem);
        }

        // Reconstruir la cola
        for (ElementoCola elem : elementos) {
            cola.encolar(elem, elem.obtenerPrioridad());
        }

        return posicion;
    }

    /**
     * Calcula el tiempo estimado de espera basado en posición y capacidad
     */
    private int calcularTiempoEstimadoEspera(ColaPrioridad<ElementoCola> cola,
            Atraccion atraccion,
            int posicion) {
        if (posicion <= 0)
            return 0;

        int capacidadCiclo = atraccion.getCapacidadMaxima() != null ? atraccion.getCapacidadMaxima()
                : CAPACIDAD_CICLO_BASE;

        int ciclosNecesarios = (int) Math.ceil((double) posicion / capacidadCiclo);
        int tiempoEstimado = ciclosNecesarios * TIEMPO_BASE_POR_PERSONA * capacidadCiclo / 60;

        return Math.max(tiempoEstimado, 1);
    }

    /**
     * Cuenta cuántas personas están delante del visitante
     */
    private int contarPersonasDelante(ColaPrioridad<ElementoCola> cola, Long visitanteId) {
        int delante = 0;

        List<ElementoCola> elementos = new ArrayList<>();
        while (!cola.estaVacia()) {
            ElementoCola elem = cola.desencolar();
            if (!elem.getVisitanteId().equals(visitanteId)) {
                delante++;
            } else {
                elementos.add(elem);
                break;
            }
            elementos.add(elem);
        }

        // Reconstruir la cola
        for (ElementoCola elem : elementos) {
            cola.encolar(elem, elem.obtenerPrioridad());
        }

        return delante;
    }

    /**
     * Calcula el tiempo promedio de espera para una atracción
     */
    private int calcularTiempoPromedioPorAtraccion(Long atraccionId) {
        Double promedio = colaVirtualRepository.obtenerTiempoPromedioEspera(atraccionId);

        if (promedio == null) {
            return 5;
        }

        return promedio.intValue();
    }

    /**
     * Genera una clave única para identificar la posición
     */
    private String generarKey(Long visitanteId, Long atraccionId) {
        return visitanteId + "_" + atraccionId;
    }

    /**
     * Construye un mensaje amigable para el visitante
     */
    private String construirMensajeCola(int posicion, int tiempoEstimado, String tipoTicket) {
        if ("FAST_PASS".equalsIgnoreCase(tipoTicket)) {
            return String.format("✅ ¡Prioridad Fast-Pass activada! Posición: %d. Tiempo estimado: %d minutos.",
                    posicion, tiempoEstimado);
        } else {
            return String.format("🕐 Te encuentras en la posición %d. Tiempo estimado de espera: %d minutos.",
                    posicion, tiempoEstimado);
        }
    }
}