package com.techpark.techpark_uq.service;

import com.techpark.techpark_uq.estructuras.ArbolBinarioBusqueda;
import com.techpark.techpark_uq.model.dto.*;
import com.techpark.techpark_uq.model.entity.*;
import com.techpark.techpark_uq.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio de Reportes y Estadísticas
 * Utiliza recorridos de estructuras de datos (ABB, Listas Enlazadas)
 * para generar reportes completos del parque
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReporteService {
    
    private final AtraccionRepository atraccionRepository;
    private final VisitanteRepository visitanteRepository;
    private final HistorialVisitaRepository historialVisitaRepository;
    private final MantenimientoRepository mantenimientoRepository;
    private final ClimaRepository climaRepository;
    private final ZonaRepository zonaRepository;
    private final UsuarioRepository usuarioRepository;
    
    // Árbol binario de búsqueda para organizar atracciones por popularidad
    private ArbolBinarioBusqueda<Integer, Atraccion> arbolPopularidad = new ArbolBinarioBusqueda<>();
    
    // ============= REPORTE DE INGRESOS =============
    
    /**
     * Generar reporte de ingresos diarios
     */
    public ReporteIngresosDTO generarReporteIngresos(LocalDate fecha) {
        log.info("💰 Generando reporte de ingresos para fecha: {}", fecha);
        
        LocalDateTime inicio = fecha.atStartOfDay();
        LocalDateTime fin = fecha.atTime(LocalTime.MAX);
        
        return generarReporteIngresosPorPeriodo(inicio, fin);
    }
    
    /**
     * Generar reporte de ingresos por período
     */
    public ReporteIngresosDTO generarReporteIngresosPorPeriodo(LocalDateTime inicio, LocalDateTime fin) {
        log.info("💰 Generando reporte de ingresos del {} al {}", inicio, fin);
        
        // Obtener visitantes que ingresaron en el período
        List<Visitante> visitantes = visitanteRepository.findAll().stream()
            .filter(v -> v.getFechaRegistro() != null)
            .filter(v -> !v.getFechaRegistro().isBefore(inicio) && !v.getFechaRegistro().isAfter(fin))
            .collect(Collectors.toList());
        
        // Calcular ingresos por tipo de ticket
        Map<String, Double> ingresosPorTipo = new HashMap<>();
        Map<String, Integer> ticketsPorTipo = new HashMap<>();
        double ingresosTotales = 0;
        
        // Precios base por tipo de ticket (en un sistema real vendrían de BD)
        Map<String, Double> preciosBase = Map.of(
            "GENERAL", 50.0,
            "FAMILIAR", 120.0,
            "FAST_PASS", 80.0
        );
        
        for (Visitante visitante : visitantes) {
            String tipoTicket = visitante.getTicketActivo() != null ? visitante.getTicketActivo() : "GENERAL";
            double precio = preciosBase.getOrDefault(tipoTicket, 50.0);
            
            ingresosTotales += precio;
            ingresosPorTipo.merge(tipoTicket, precio, Double::sum);
            ticketsPorTipo.merge(tipoTicket, 1, Integer::sum);
        }
        
        // Calcular ingresos adicionales por atracciones con costo extra
        List<HistorialVisita> visitas = historialVisitaRepository.findAll().stream()
            .filter(v -> v.getFechaVisita() != null)
            .filter(v -> !v.getFechaVisita().isBefore(inicio) && !v.getFechaVisita().isAfter(fin))
            .collect(Collectors.toList());
        
        Map<String, Double> ingresosPorAtraccion = new HashMap<>();
        for (HistorialVisita visita : visitas) {
            Atraccion atraccion = visita.getAtraccion();
            if (atraccion.getCostoAdicional() != null && atraccion.getCostoAdicional() > 0) {
                Visitante visitante = visita.getVisitante();
                if (visitante != null && !"FAST_PASS".equalsIgnoreCase(visitante.getTicketActivo())) {
                    ingresosTotales += atraccion.getCostoAdicional();
                    ingresosPorAtraccion.merge(atraccion.getNombre(), atraccion.getCostoAdicional(), Double::sum);
                }
            }
        }
        
        // Calcular crecimiento respecto al período anterior
        long diasPeriodo = java.time.Duration.between(inicio, fin).toDays();
        LocalDateTime inicioAnterior = inicio.minusDays(diasPeriodo + 1);
        LocalDateTime finAnterior = inicio.minusDays(1);
        
        List<Visitante> visitantesAnteriores = visitanteRepository.findAll().stream()
            .filter(v -> v.getFechaRegistro() != null)
            .filter(v -> !v.getFechaRegistro().isBefore(inicioAnterior) && !v.getFechaRegistro().isAfter(finAnterior))
            .collect(Collectors.toList());
        
        double ingresosAnteriores = visitantesAnteriores.size() * 50.0; // Estimación
        double crecimiento = ingresosAnteriores > 0 ? 
            ((ingresosTotales - ingresosAnteriores) / ingresosAnteriores) * 100 : 0;
        
        String tendencia = crecimiento > 5 ? "CRECIMIENTO" : (crecimiento < -5 ? "DECRECIMIENTO" : "ESTABLE");
        
        return ReporteIngresosDTO.builder()
            .fechaInicio(inicio)
            .fechaFin(fin)
            .ingresosTotales(ingresosTotales)
            .totalVisitantes(visitantes.size())
            .promedioPorVisitante(visitantes.isEmpty() ? 0 : ingresosTotales / visitantes.size())
            .ingresosPorTipoTicket(ingresosPorTipo)
            .ticketsVendidosPorTipo(ticketsPorTipo)
            .ingresosPorAtraccion(ingresosPorAtraccion)
            .crecimientoPorcentual(crecimiento)
            .tendencia(tendencia)
            .build();
    }
    
    // ============= REPORTE DE ATRACCIONES =============
    
    /**
     * Generar reporte de atracciones más y menos visitadas
     * Utiliza Árbol Binario de Búsqueda para ordenar por popularidad
     */
    public ReporteAtraccionesDTO generarReporteAtracciones() {
        log.info("🎢 Generando reporte de atracciones");
        
        List<Atraccion> atracciones = atraccionRepository.findAll();
        
        // Limpiar y reconstruir el árbol de popularidad
        arbolPopularidad = new ArbolBinarioBusqueda<>();
        
        // Insertar cada atracción en el ABB usando su contador como clave
        for (Atraccion atraccion : atracciones) {
            arbolPopularidad.insertar(atraccion.getContadorVisitantes(), atraccion);
        }
        
        // Recorrido in-order para obtener atracciones en orden ascendente
        List<ArbolBinarioBusqueda.Entry<Integer, Atraccion>> ordenadas = arbolPopularidad.inOrder();
        
        // Las más visitadas son las últimas (mayor contador)
        List<ReporteAtraccionesDTO.AtraccionEstadisticasDTO> masVisitadas = new ArrayList<>();
        List<ReporteAtraccionesDTO.AtraccionEstadisticasDTO> menosVisitadas = new ArrayList<>();
        
        int totalVisitantes = 0;
        
        for (int i = 0; i < ordenadas.size(); i++) {
            var entry = ordenadas.get(i);
            Atraccion atraccion = entry.getValue();
            int visitantes = entry.getKey();
            totalVisitantes += visitantes;
            
            ReporteAtraccionesDTO.AtraccionEstadisticasDTO stats = construirEstadisticasAtraccion(atraccion);
            
            // Las 5 más visitadas (últimas 5)
            if (i >= ordenadas.size() - 5) {
                masVisitadas.add(stats);
            }
            
            // Las 5 menos visitadas (primeras 5)
            if (i < 5) {
                menosVisitadas.add(stats);
            }
        }
        
        // Ordenar más visitadas de mayor a menor
        masVisitadas.sort((a, b) -> b.getTotalVisitantes().compareTo(a.getTotalVisitantes()));
        
        double promedio = atracciones.isEmpty() ? 0 : (double) totalVisitantes / atracciones.size();
        
        // Identificar la atracción estrella (la más visitada)
        String atraccionEstrella = masVisitadas.isEmpty() ? "N/A" : masVisitadas.get(0).getNombre();
        
        return ReporteAtraccionesDTO.builder()
            .atraccionesMasVisitadas(masVisitadas)
            .atraccionesMenosVisitadas(menosVisitadas)
            .promedioVisitantesPorAtraccion(promedio)
            .totalVisitasParque(totalVisitantes)
            .atraccionEstrella(atraccionEstrella)
            .build();
    }
    
    /**
     * Construye estadísticas detalladas de una atracción
     */
    private ReporteAtraccionesDTO.AtraccionEstadisticasDTO construirEstadisticasAtraccion(Atraccion atraccion) {
        // Calcular popularidad sobre 100%
        int capacidadTotal = atraccion.getCapacidadMaxima() != null ? atraccion.getCapacidadMaxima() * 10 : 200;
        double popularidad = Math.min(100.0, (double) atraccion.getContadorVisitantes() / capacidadTotal * 100);
        
        // Contar mantenimientos de esta atracción
        int mantenimientos = mantenimientoRepository.findByAtraccionIdOrderByFechaGeneracionDesc(atraccion.getId()).size();
        
        return ReporteAtraccionesDTO.AtraccionEstadisticasDTO.builder()
            .id(atraccion.getId())
            .nombre(atraccion.getNombre())
            .zona(atraccion.getZona() != null ? atraccion.getZona().getNombre() : "Sin zona")
            .totalVisitantes(atraccion.getContadorVisitantes())
            .tiempoPromedioEspera(atraccion.getTiempoEsperaEstimado())
            .porcentajeOcupacion(calcularOcupacionAtraccion(atraccion))
            .numeroMantenimientos(mantenimientos)
            .popularidad(popularidad)
            .build();
    }
    
    private double calcularOcupacionAtraccion(Atraccion atraccion) {
        int capacidad = atraccion.getCapacidadMaxima() != null ? atraccion.getCapacidadMaxima() : 20;
        int visitantes = atraccion.getContadorVisitantes() % capacidad;
        return (double) visitantes / capacidad * 100;
    }
    
    // ============= REPORTE DE TIEMPOS DE ESPERA =============
    
    /**
     * Generar reporte de tiempos de espera
     */
    public ReporteTiemposDTO generarReporteTiempos(LocalDate fecha) {
        log.info("⏱️ Generando reporte de tiempos de espera para fecha: {}", fecha);
        
        LocalDateTime inicio = fecha.atStartOfDay();
        LocalDateTime fin = fecha.atTime(LocalTime.MAX);
        
        List<HistorialVisita> visitas = historialVisitaRepository.findAll().stream()
            .filter(v -> v.getFechaVisita() != null)
            .filter(v -> !v.getFechaVisita().isBefore(inicio) && !v.getFechaVisita().isAfter(fin))
            .collect(Collectors.toList());
        
        Map<String, Double> tiemposPorAtraccion = new HashMap<>();
        Map<Integer, Double> tiemposPorHora = new HashMap<>();
        Map<String, Double> tiemposPorTipo = new HashMap<>();
        
        List<ReporteTiemposDTO.AtraccionCuelloBotellaDTO> cuellosDeBotella = new ArrayList<>();
        
        int tiempoMax = 0;
        int tiempoMin = Integer.MAX_VALUE;
        int totalTiempos = 0;
        int count = 0;
        
        for (HistorialVisita visita : visitas) {
            int tiempo = visita.getTiempoEsperaReal() != null ? visita.getTiempoEsperaReal() : 0;
            
            if (tiempo > 0) {
                totalTiempos += tiempo;
                count++;
                tiempoMax = Math.max(tiempoMax, tiempo);
                tiempoMin = Math.min(tiempoMin, tiempo);
                
                // Por atracción
                String nombreAtraccion = visita.getAtraccion().getNombre();
                tiemposPorAtraccion.merge(nombreAtraccion, (double) tiempo, Double::sum);
                
                // Por hora
                int hora = visita.getFechaVisita().getHour();
                tiemposPorHora.merge(hora, (double) tiempo, Double::sum);
                
                // Por tipo de ticket
                String tipoTicket = visita.getUsoFastPass() ? "FAST_PASS" : "GENERAL";
                tiemposPorTipo.merge(tipoTicket, (double) tiempo, Double::sum);
            }
        }
        
        // Calcular promedios
        for (Map.Entry<String, Double> entry : tiemposPorAtraccion.entrySet()) {
            int countByAtraccion = (int) visitas.stream()
                .filter(v -> v.getAtraccion().getNombre().equals(entry.getKey()))
                .count();
            double promedio = countByAtraccion > 0 ? entry.getValue() / countByAtraccion : 0;
            entry.setValue(promedio);
            
            // Identificar cuellos de botella (tiempo > 30 min)
            if (promedio > 30) {
                Atraccion atraccion = atraccionRepository.findAll().stream()
                    .filter(a -> a.getNombre().equals(entry.getKey()))
                    .findFirst().orElse(null);
                
                if (atraccion != null) {
                    int capacidad = atraccion.getCapacidadMaxima() != null ? atraccion.getCapacidadMaxima() : 20;
                    cuellosDeBotella.add(ReporteTiemposDTO.AtraccionCuelloBotellaDTO.builder()
                        .nombre(entry.getKey())
                        .tiempoPromedio((int) Math.round(promedio))
                        .capacidad(capacidad)
                        .visitantesPorHora(atraccion.getContadorVisitantes() / 8) // Estimado
                        .recomendacion(generarRecomendacionCuelloBotella(promedio, capacidad))
                        .build());
                }
            }
        }
        
        double tiempoPromedioGeneral = count > 0 ? (double) totalTiempos / count : 0;
        
        return ReporteTiemposDTO.builder()
            .tiempoPromedioGeneral(tiempoPromedioGeneral)
            .tiempoMaximoRegistrado(tiempoMax)
            .tiempoMinimoRegistrado(tiempoMin == Integer.MAX_VALUE ? 0 : tiempoMin)
            .tiemposPromedioPorAtraccion(tiemposPorAtraccion)
            .tiemposPorHora(tiemposPorHora)
            .tiemposPorTipoTicket(tiemposPorTipo)
            .cuellosDeBotella(cuellosDeBotella)
            .build();
    }
    
    private String generarRecomendacionCuelloBotella(double tiempoPromedio, int capacidad) {
        if (tiempoPromedio > 60) {
            return "URGENTE: Considerar aumentar capacidad o agregar más personal";
        } else if (tiempoPromedio > 40) {
            return "Mejorar eficiencia del ciclo de la atracción";
        } else {
            return "Optimizar proceso de carga y descarga de visitantes";
        }
    }
    
    // ============= REPORTE DE MANTENIMIENTO =============
    
    /**
     * Generar reporte de mantenimiento
     */
    public ReporteMantenimientoDTO generarReporteMantenimiento() {
        log.info("🔧 Generando reporte de mantenimiento");
        
        List<Mantenimiento> mantenimientos = mantenimientoRepository.findAll();
        
        Map<String, Integer> mantenimientosPorTipo = new HashMap<>();
        Map<String, Integer> mantenimientosPorPrioridad = new HashMap<>();
        Map<String, Integer> cierresPorCausa = new HashMap<>();
        
        long pendientes = mantenimientos.stream().filter(m -> "PENDIENTE".equals(m.getEstado())).count();
        long completados = mantenimientos.stream().filter(m -> "RESUELTA".equals(m.getEstado())).count();
        
        double tiempoPromedio = 0;
        int tiempoTotal = 0;
        int tiempoCount = 0;
        
        for (Mantenimiento m : mantenimientos) {
            // Por tipo
            mantenimientosPorTipo.merge(m.getTipoAlerta(), 1, Integer::sum);
            
            // Por prioridad
            mantenimientosPorPrioridad.merge(m.getPrioridad(), 1, Integer::sum);
            
            // Tiempo promedio
            if (m.getTiempoResolucionMinutos() != null) {
                tiempoTotal += m.getTiempoResolucionMinutos();
                tiempoCount++;
            }
        }
        
        tiempoPromedio = tiempoCount > 0 ? (double) tiempoTotal / tiempoCount : 0;
        
        // Top atracciones con más incidentes
        List<Object[]> incidentesPorAtraccion = mantenimientoRepository.findAtraccionesConMasIncidentes();
        List<ReporteMantenimientoDTO.AtraccionIncidentesDTO> atraccionesIncidentes = new ArrayList<>();
        
        for (Object[] row : incidentesPorAtraccion) {
            String nombre = (String) row[0];
            Long incidentes = (Long) row[1];
            
            atraccionesIncidentes.add(ReporteMantenimientoDTO.AtraccionIncidentesDTO.builder()
                .nombre(nombre)
                .incidentes(incidentes.intValue())
                .tiempoPromedioResolucion(tiempoPromedio)
                .build());
        }
        
        // Alertas climáticas
        long alertasClima = climaRepository.count();
        cierresPorCausa.put("Clima", (int) alertasClima);
        cierresPorCausa.put("Mantenimiento", (int) pendientes);
        cierresPorCausa.put("Operativo", 0); // Se podría calcular
        
        return ReporteMantenimientoDTO.builder()
            .totalMantenimientos(mantenimientos.size())
            .mantenimientosPendientes((int) pendientes)
            .mantenimientosCompletados((int) completados)
            .tiempoPromedioResolucionMinutos(tiempoPromedio)
            .mantenimientosPorTipo(mantenimientosPorTipo)
            .mantenimientosPorPrioridad(mantenimientosPorPrioridad)
            .atraccionesMasIncidentes(atraccionesIncidentes)
            .alertasClimaGeneradas((int) alertasClima)
            .cierresPorCausa(cierresPorCausa)
            .build();
    }
    
    // ============= REPORTE DE AFLUENCIA =============
    
    /**
     * Generar reporte de afluencia de visitantes
     */
    public ReporteAfluenciaDTO generarReporteAfluencia(LocalDate fecha) {
        log.info("👥 Generando reporte de afluencia para fecha: {}", fecha);
        
        LocalDateTime inicio = fecha.atStartOfDay();
        LocalDateTime fin = fecha.atTime(LocalTime.MAX);
        
        List<Visitante> visitantes = visitanteRepository.findAll().stream()
            .filter(v -> v.getFechaRegistro() != null)
            .filter(v -> !v.getFechaRegistro().isBefore(inicio) && !v.getFechaRegistro().isAfter(fin))
            .collect(Collectors.toList());
        
        // Capacidad máxima del parque (estimada)
        int capacidadMaxima = 5000;
        int totalVisitantes = visitantes.size();
        double porcentajeOcupacion = (double) totalVisitantes / capacidadMaxima * 100;
        
        String nivelAfluencia;
        if (porcentajeOcupacion < 30) {
            nivelAfluencia = "BAJA";
        } else if (porcentajeOcupacion < 60) {
            nivelAfluencia = "MEDIA";
        } else if (porcentajeOcupacion < 85) {
            nivelAfluencia = "ALTA";
        } else {
            nivelAfluencia = "LLENO";
        }
        
        // Visitantes por zona
        Map<String, Integer> visitantesPorZona = new HashMap<>();
        Map<String, Double> ocupacionPorZona = new HashMap<>();
        
        List<Zona> zonas = zonaRepository.findAll();
        for (Zona zona : zonas) {
            int visitantesZona = 0;
            for (Atraccion atraccion : zona.getAtracciones()) {
                visitantesZona += atraccion.getContadorVisitantes();
            }
            visitantesPorZona.put(zona.getNombre(), visitantesZona);
            
            int capacidadZona = zona.getCapacidadMaxima() != null ? zona.getCapacidadMaxima() : 1000;
            ocupacionPorZona.put(zona.getNombre(), (double) visitantesZona / capacidadZona * 100);
        }
        
        // Visitantes por hora
        Map<Integer, Integer> visitantesPorHora = new HashMap<>();
        for (int hora = 0; hora < 24; hora++) {
            int finalHora = hora;
            long count = historialVisitaRepository.findAll().stream()
                .filter(v -> v.getFechaVisita() != null)
                .filter(v -> v.getFechaVisita().getHour() == finalHora)
                .filter(v -> !v.getFechaVisita().isBefore(inicio) && !v.getFechaVisita().isAfter(fin))
                .count();
            visitantesPorHora.put(hora, (int) count);
        }
        
        // Identificar horas pico
        List<Integer> horasPico = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : visitantesPorHora.entrySet()) {
            if (entry.getValue() > 100) {  // Umbral de hora pico
                horasPico.add(entry.getKey());
            }
        }
        horasPico.sort(Integer::compareTo);
        
        // Recomendaciones
        List<String> recomendaciones = new ArrayList<>();
        if (nivelAfluencia.equals("ALTA") || nivelAfluencia.equals("LLENO")) {
            recomendaciones.add("⚠️ Alta afluencia detectada. Considere abrir más taquillas.");
            recomendaciones.add("📢 Active personal adicional en atracciones populares.");
        } else if (nivelAfluencia.equals("BAJA")) {
            recomendaciones.add("💡 Afluencia baja. Buen momento para realizar mantenimiento preventivo.");
            recomendaciones.add("🎟️ Considere ofrecer promociones para aumentar visitantes.");
        }
        
        if (!horasPico.isEmpty()) {
            recomendaciones.add("⏰ Horas pico: " + horasPico + ". Refuerce personal en estos horarios.");
        }
        
        return ReporteAfluenciaDTO.builder()
            .totalVisitantesDia(totalVisitantes)
            .capacidadMaximaParque(capacidadMaxima)
            .porcentajeOcupacion(porcentajeOcupacion)
            .nivelAfluencia(nivelAfluencia)
            .visitantesPorZona(visitantesPorZona)
            .ocupacionPorZona(ocupacionPorZona)
            .visitantesPorHora(visitantesPorHora)
            .horasPico(horasPico)
            .tendenciaPorcentual(calcularTendenciaAfluencia(fecha))
            .recomendaciones(recomendaciones)
            .build();
    }
    
    private Double calcularTendenciaAfluencia(LocalDate fecha) {
        // Comparar con el día anterior
        LocalDate diaAnterior = fecha.minusDays(1);
        
        LocalDateTime inicioHoy = fecha.atStartOfDay();
        LocalDateTime finHoy = fecha.atTime(LocalTime.MAX);
        LocalDateTime inicioAyer = diaAnterior.atStartOfDay();
        LocalDateTime finAyer = diaAnterior.atTime(LocalTime.MAX);
        
        long hoy = visitanteRepository.findAll().stream()
            .filter(v -> v.getFechaRegistro() != null)
            .filter(v -> !v.getFechaRegistro().isBefore(inicioHoy) && !v.getFechaRegistro().isAfter(finHoy))
            .count();
        
        long ayer = visitanteRepository.findAll().stream()
            .filter(v -> v.getFechaRegistro() != null)
            .filter(v -> !v.getFechaRegistro().isBefore(inicioAyer) && !v.getFechaRegistro().isAfter(finAyer))
            .count();
        
        if (ayer == 0) return 0.0;
        return ((double) (hoy - ayer) / ayer) * 100;
    }
    
    // ============= REPORTE GENERAL =============
    
    /**
     * Generar reporte general completo del parque
     */
    public ReporteGeneralDTO generarReporteGeneral(String periodo) {
        log.info("📊 Generando reporte general periodo: {}", periodo);
        
        LocalDateTime fechaInicio;
        LocalDateTime fechaFin = LocalDateTime.now();
        
        switch (periodo.toUpperCase()) {
            case "DIARIO":
                fechaInicio = fechaFin.minusDays(1);
                break;
            case "SEMANAL":
                fechaInicio = fechaFin.minusWeeks(1);
                break;
            case "MENSUAL":
                fechaInicio = fechaFin.minusMonths(1);
                break;
            default:
                fechaInicio = fechaFin.minusDays(1);
        }
        
        // Generar todos los reportes
        ReporteIngresosDTO ingresos = generarReporteIngresosPorPeriodo(fechaInicio, fechaFin);
        ReporteAtraccionesDTO atracciones = generarReporteAtracciones();
        ReporteTiemposDTO tiempos = generarReporteTiempos(fechaFin.toLocalDate());
        ReporteMantenimientoDTO mantenimiento = generarReporteMantenimiento();
        ReporteAfluenciaDTO afluencia = generarReporteAfluencia(fechaFin.toLocalDate());
        
        // Calcular calificación general (0-100)
        int calificacion = calcularCalificacionGeneral(ingresos, atracciones, tiempos, mantenimiento, afluencia);
        
        // Generar resumen ejecutivo
        String resumenEjecutivo = generarResumenEjecutivo(ingresos, atracciones, tiempos, afluencia);
        
        // Identificar puntos fuertes y áreas de mejora
        List<String> puntosFuertes = new ArrayList<>();
        List<String> areasMejora = new ArrayList<>();
        
        if (atracciones.getAtraccionEstrella() != null) {
            puntosFuertes.add("✨ Atracción estrella: " + atracciones.getAtraccionEstrella());
        }
        
        if (ingresos.getCrecimientoPorcentual() > 0) {
            puntosFuertes.add(String.format("📈 Crecimiento de ingresos: +%.1f%%", ingresos.getCrecimientoPorcentual()));
        }
        
        if (!tiempos.getCuellosDeBotella().isEmpty()) {
            areasMejora.add("⚠️ Cuellos de botella identificados en: " + 
                tiempos.getCuellosDeBotella().stream().map(c -> c.getNombre()).limit(3).collect(Collectors.joining(", ")));
        }
        
        if (mantenimiento.getMantenimientosPendientes() > 5) {
            areasMejora.add("🔧 Hay " + mantenimiento.getMantenimientosPendientes() + " mantenimientos pendientes");
        }
        
        return ReporteGeneralDTO.builder()
            .fechaGeneracion(LocalDateTime.now())
            .periodo(periodo)
            .resumenEjecutivo(resumenEjecutivo)
            .ingresos(ingresos)
            .atracciones(atracciones)
            .tiempos(tiempos)
            .mantenimiento(mantenimiento)
            .afluencia(afluencia)
            .calificacionGeneral(calificacion)
            .puntosFuertes(puntosFuertes)
            .areasMejora(areasMejora)
            .build();
    }
    
    private int calcularCalificacionGeneral(ReporteIngresosDTO ingresos, ReporteAtraccionesDTO atracciones,
                                            ReporteTiemposDTO tiempos, ReporteMantenimientoDTO mantenimiento,
                                            ReporteAfluenciaDTO afluencia) {
        int puntaje = 0;
        
        // Basado en ingresos (hasta 30 puntos)
        if (ingresos.getCrecimientoPorcentual() > 10) puntaje += 30;
        else if (ingresos.getCrecimientoPorcentual() > 0) puntaje += 20;
        else if (ingresos.getCrecimientoPorcentual() > -10) puntaje += 10;
        
        // Basado en tiempos de espera (hasta 30 puntos)
        if (tiempos.getTiempoPromedioGeneral() < 15) puntaje += 30;
        else if (tiempos.getTiempoPromedioGeneral() < 30) puntaje += 15;
        else puntaje += 5;
        
        // Basado en mantenimiento (hasta 20 puntos)
        if (mantenimiento.getMantenimientosPendientes() == 0) puntaje += 20;
        else if (mantenimiento.getMantenimientosPendientes() < 3) puntaje += 10;
        
        // Basado en afluencia (hasta 20 puntos)
        if (afluencia.getNivelAfluencia().equals("MEDIA")) puntaje += 20;
        else if (afluencia.getNivelAfluencia().equals("ALTA")) puntaje += 15;
        else puntaje += 10;
        
        return puntaje;
    }
    
    private String generarResumenEjecutivo(ReporteIngresosDTO ingresos, ReporteAtraccionesDTO atracciones,
                                           ReporteTiemposDTO tiempos, ReporteAfluenciaDTO afluencia) {
        return String.format(
            "📋 RESUMEN EJECUTIVO\n" +
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
            "💰 Ingresos totales: $%.2f\n" +
            "👥 Visitantes: %d\n" +
            "⭐ Atracción más popular: %s\n" +
            "⏱️ Tiempo promedio de espera: %.1f min\n" +
            "📊 Nivel de afluencia: %s\n" +
            "📈 Tendencia de ingresos: %s\n" +
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
            ingresos.getIngresosTotales(),
            ingresos.getTotalVisitantes(),
            atracciones.getAtraccionEstrella(),
            tiempos.getTiempoPromedioGeneral(),
            afluencia.getNivelAfluencia(),
            ingresos.getTendencia()
        );
    }
}