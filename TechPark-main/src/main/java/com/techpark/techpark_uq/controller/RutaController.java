package com.techpark.techpark_uq.controller;

import com.techpark.techpark_uq.model.dto.ApiResponseDTO;
import com.techpark.techpark_uq.model.dto.RutaDTO;
import com.techpark.techpark_uq.model.dto.RutaMultipleDTO;
import com.techpark.techpark_uq.model.dto.SolicitudRutaDTO;
import com.techpark.techpark_uq.model.entity.Atraccion;
import com.techpark.techpark_uq.repository.AtraccionRepository;
import com.techpark.techpark_uq.service.RutaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rutas")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RutaController {

    private final RutaService rutaService;
    private final AtraccionRepository atraccionRepository; // ← Agregar esta línea

    @PostMapping("/mas-corta")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponseDTO<RutaDTO>> encontrarRutaMasCorta(
            @Valid @RequestBody SolicitudRutaDTO solicitud,
            HttpServletRequest request) {

        RutaDTO ruta = rutaService.encontrarRutaMasCorta(solicitud);

        return ResponseEntity.ok(ApiResponseDTO.success(
                ruta,
                "Ruta encontrada exitosamente",
                request.getRequestURI()));
    }

    @PostMapping("/multiple")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponseDTO<RutaMultipleDTO>> planificarRutaMultiple(
            @RequestBody List<Long> atraccionesIds,
            HttpServletRequest request) {

        RutaMultipleDTO rutaMultiple = rutaService.encontrarRutaMultiple(atraccionesIds);

        return ResponseEntity.ok(ApiResponseDTO.success(
                rutaMultiple,
                "Ruta múltiple planificada",
                request.getRequestURI()));
    }

    @GetMapping("/cercanas/{atraccionId}/{limite}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponseDTO<List<Map<String, Object>>>> encontrarCercanas(
            @PathVariable Long atraccionId,
            @PathVariable int limite,
            HttpServletRequest request) {

        List<Map<String, Object>> cercanas = rutaService.encontrarAtraccionesCercanas(atraccionId, limite);

        return ResponseEntity.ok(ApiResponseDTO.success(
                cercanas,
                "Atracciones cercanas encontradas",
                request.getRequestURI()));
    }

    @GetMapping("/estado-mapa")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> obtenerEstadoMapa(
            HttpServletRequest request) {

        Map<String, Object> estado = rutaService.obtenerEstadoMapa();

        return ResponseEntity.ok(ApiResponseDTO.success(
                estado,
                "Estado del mapa obtenido",
                request.getRequestURI()));
    }

    @GetMapping("/mapa-visual")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> obtenerMapaVisual(
            HttpServletRequest request) {

        Map<String, Object> mapa = rutaService.obtenerMapaVisual();

        return ResponseEntity.ok(ApiResponseDTO.success(
                mapa,
                "Mapa visual generado",
                request.getRequestURI()));
    }

    @PostMapping("/refrescar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponseDTO<String>> refrescarMapa(
            HttpServletRequest request) {

        rutaService.refrescarMapa();

        return ResponseEntity.ok(ApiResponseDTO.success(
                "Mapa refrescado exitosamente",
                "Mapa actualizado con las nuevas atracciones",
                request.getRequestURI()));
    }

    @GetMapping("/grafo-completo")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> obtenerGrafoCompleto(
            HttpServletRequest request) {

        Map<String, Object> grafoData = new HashMap<>();

        // Obtener todas las atracciones (nodos)
        List<Atraccion> atracciones = atraccionRepository.findAll();
        List<Map<String, Object>> nodos = new ArrayList<>();

        for (Atraccion a : atracciones) {
            Map<String, Object> nodo = new HashMap<>();
            nodo.put("id", a.getId());
            nodo.put("nombre", a.getNombre());
            nodo.put("tipo", a.getTipo().toString());
            nodo.put("posicionX", a.getPosicionX());
            nodo.put("posicionY", a.getPosicionY());
            nodo.put("estado", a.getEstado().toString());
            nodos.add(nodo);
        }

        grafoData.put("nodos", nodos);
        grafoData.put("totalNodos", nodos.size());

        return ResponseEntity.ok(ApiResponseDTO.success(
                grafoData,
                "Grafo obtenido exitosamente",
                request.getRequestURI()));
    }

    @GetMapping("/posiciones")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponseDTO<Map<Long, Map<String, Double>>>> obtenerPosiciones() {
        Map<Long, Map<String, Double>> posiciones = rutaService.obtenerPosicionesReales();
        return ResponseEntity.ok(ApiResponseDTO.success(posiciones, "Posiciones obtenidas", "/api/rutas/posiciones"));
    }
}