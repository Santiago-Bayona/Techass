package com.techpark.techpark_uq.service;

import com.techpark.techpark_uq.model.dto.HistorialVisitaDTO;
import com.techpark.techpark_uq.model.entity.HistorialVisita;
import com.techpark.techpark_uq.repository.HistorialVisitaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HistorialVisitaService {

    private final HistorialVisitaRepository historialVisitaRepository;

    /**
     * Obtener historial de visitas con paginación
     */
    public Page<HistorialVisitaDTO> obtenerHistorialPaginado(Long visitanteId, Pageable pageable) {
        log.info("📜 Obteniendo historial paginado para visitante: {}", visitanteId);

        List<HistorialVisita> todosLos = historialVisitaRepository.findByVisitanteIdOrderByFechaVisitaAsc(visitanteId);

        // Aplicar paginación manualmente
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), todosLos.size());

        List<HistorialVisitaDTO> contenido = todosLos.subList(start, end)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());

        return new PageImpl<>(contenido, pageable, todosLos.size());
    }

    /**
     * Obtener todos los historiales sin paginación
     */
    public List<HistorialVisitaDTO> obtenerTodosLosHistoriales(Long visitanteId) {
        log.info("📜 Obteniendo todos los historiales para visitante: {}", visitanteId);

        return historialVisitaRepository.findByVisitanteIdOrderByFechaVisitaAsc(visitanteId)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtener estadísticas del visitante
     */
    public java.util.Map<String, Object> obtenerEstadisticas(Long visitanteId) {
        log.info("📊 Obteniendo estadísticas para visitante: {}", visitanteId);

        List<HistorialVisita> historial = historialVisitaRepository.findByVisitanteIdOrderByFechaVisitaAsc(visitanteId);

        int totalVisitas = historial.size();
        double tiempoPromedioEspera = historial.isEmpty() ? 0 :
                historial.stream()
                        .mapToInt(h -> h.getTiempoEsperaReal() != null ? h.getTiempoEsperaReal() : 0)
                        .average()
                        .orElse(0.0);

        long usosLastPass = historial.stream()
                .filter(h -> h.getUsoFastPass() != null && h.getUsoFastPass())
                .count();

        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("totalVisitas", totalVisitas);
        stats.put("tiempoPromedioEspera", Math.round(tiempoPromedioEspera));
        stats.put("usosLastPass", usosLastPass);

        return stats;
    }

    /**
     * Convertir entidad a DTO
     */
    private HistorialVisitaDTO convertirADTO(HistorialVisita historial) {
        return HistorialVisitaDTO.builder()
                .id(historial.getId())
                .atraccionId(historial.getAtraccion().getId())
                .atraccionNombre(historial.getAtraccion().getNombre())
                .atraccionTipo(historial.getAtraccion().getTipo().toString())
                .fechaVisita(historial.getFechaVisita())
                .tiempoEsperaReal(historial.getTiempoEsperaReal())
                .usoFastPass(historial.getUsoFastPass())
                .build();
    }
}
