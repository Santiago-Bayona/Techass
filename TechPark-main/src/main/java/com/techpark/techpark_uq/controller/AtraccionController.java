package com.techpark.techpark_uq.controller;

import com.techpark.techpark_uq.model.dto.ApiResponseDTO;
import com.techpark.techpark_uq.model.dto.AtraccionDTO;
import com.techpark.techpark_uq.model.dto.HistorialVisitaDTO;
import com.techpark.techpark_uq.model.entity.HistorialVisita;
import com.techpark.techpark_uq.service.AtraccionService;
import com.techpark.techpark_uq.service.HistorialVisitaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/atracciones")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AtraccionController {

    private final AtraccionService atraccionService;
    private final HistorialVisitaService historialVisitaService;

    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<AtraccionDTO>>> listarTodas(
            HttpServletRequest request) {
        List<AtraccionDTO> atracciones = atraccionService.listarTodas();
        return ResponseEntity.ok(ApiResponseDTO.success(
            atracciones,
            "Atracciones listadas exitosamente",
            request.getRequestURI()
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<AtraccionDTO>> obtenerPorId(
            @PathVariable Long id,
            HttpServletRequest request) {
        AtraccionDTO atraccion = atraccionService.obtenerPorId(id);
        return ResponseEntity.ok(ApiResponseDTO.success(
            atraccion,
            "Atracción encontrada",
            request.getRequestURI()
        ));
    }

    @PostMapping
    public ResponseEntity<ApiResponseDTO<AtraccionDTO>> crear(
            @Valid @RequestBody AtraccionDTO atraccionDTO,
            HttpServletRequest request) {
        AtraccionDTO nueva = atraccionService.crear(atraccionDTO);
        return ResponseEntity.ok(ApiResponseDTO.success(
            nueva,
            "Atracción creada exitosamente",
            request.getRequestURI()
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<AtraccionDTO>> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody AtraccionDTO atraccionDTO,
            HttpServletRequest request) {
        AtraccionDTO actualizada = atraccionService.actualizar(id, atraccionDTO);
        return ResponseEntity.ok(ApiResponseDTO.success(
            actualizada,
            "Atracción actualizada exitosamente",
            request.getRequestURI()
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> eliminar(
            @PathVariable Long id,
            HttpServletRequest request) {
        atraccionService.eliminar(id);
        return ResponseEntity.ok(ApiResponseDTO.success(
            null,
            "Atracción eliminada exitosamente",
            request.getRequestURI()
        ));
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<ApiResponseDTO<List<AtraccionDTO>>> listarPorEstado(
            @PathVariable String estado,
            HttpServletRequest request) {
        List<AtraccionDTO> atracciones = atraccionService.listarPorEstado(estado);
        return ResponseEntity.ok(ApiResponseDTO.success(
            atracciones,
            "Atracciones filtradas por estado",
            request.getRequestURI()
        ));
    }

    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<ApiResponseDTO<List<AtraccionDTO>>> listarPorTipo(
            @PathVariable String tipo,
            HttpServletRequest request) {
        List<AtraccionDTO> atracciones = atraccionService.listarPorTipo(tipo);
        return ResponseEntity.ok(ApiResponseDTO.success(
            atracciones,
            "Atracciones filtradas por tipo",
            request.getRequestURI()
        ));
    }

    @GetMapping("/zona/{zonaId}")
    public ResponseEntity<ApiResponseDTO<List<AtraccionDTO>>> listarPorZona(
            @PathVariable Long zonaId,
            HttpServletRequest request) {
        List<AtraccionDTO> atracciones = atraccionService.listarPorZona(zonaId);
        return ResponseEntity.ok(ApiResponseDTO.success(
            atracciones,
            "Atracciones filtradas por zona",
            request.getRequestURI()
        ));
    }

    /**
     * Obtener favoritos del visitante
     */
    @GetMapping("/favoritos/{visitanteId}")
    @PreAuthorize("hasRole('VISITANTE')")
    public ResponseEntity<ApiResponseDTO<List<AtraccionDTO>>> obtenerFavoritos(
            @PathVariable Long visitanteId,
            HttpServletRequest request) {

        System.out.println("⭐ Obteniendo favoritos del visitante: " + visitanteId);

        List<AtraccionDTO> favoritos = atraccionService.obtenerFavoritos(visitanteId);

        return ResponseEntity.ok(ApiResponseDTO.success(
                favoritos,
                "Favoritos obtenidos exitosamente",
                request.getRequestURI()
        ));
    }

    /**
     * Agregar a favoritos
     */
    @PostMapping("/favorito/{visitanteId}/{atraccionId}")
    @PreAuthorize("hasRole('VISITANTE')")
    public ResponseEntity<ApiResponseDTO<Void>> agregarFavorito(
            @PathVariable Long visitanteId,
            @PathVariable Long atraccionId,
            HttpServletRequest request) {

        System.out.println("⭐ Agregando atracción " + atraccionId + " a favoritos del visitante " + visitanteId);

        atraccionService.agregarFavorito(visitanteId, atraccionId);

        return ResponseEntity.ok(ApiResponseDTO.success(
                null,
                "Agregado a favoritos",
                request.getRequestURI()
        ));
    }

    /**
     * Remover de favoritos
     */
    @DeleteMapping("/favorito/{visitanteId}/{atraccionId}")
    @PreAuthorize("hasRole('VISITANTE')")
    public ResponseEntity<ApiResponseDTO<Void>> removerFavorito(
            @PathVariable Long visitanteId,
            @PathVariable Long atraccionId,
            HttpServletRequest request) {

        System.out.println("❌ Removiendo atracción " + atraccionId + " de favoritos del visitante " + visitanteId);

        atraccionService.removerFavorito(visitanteId, atraccionId);

        return ResponseEntity.ok(ApiResponseDTO.success(
                null,
                "Removido de favoritos",
                request.getRequestURI()
        ));
    }

    /**
     * Crear nueva HistorialController
     */
    /**
     * Obtener historial con paginación
     */
    @GetMapping("/historial/{visitanteId}")
    @PreAuthorize("hasRole('VISITANTE')")
    public ResponseEntity<ApiResponseDTO<Page<HistorialVisitaDTO>>> obtenerHistorial(
            @PathVariable Long visitanteId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            HttpServletRequest request) {

        System.out.println("📜 Obteniendo historial del visitante: " + visitanteId);

        // ✅ Orden: más viejo -> más reciente
        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaVisita").ascending());

        Page<HistorialVisitaDTO> historialPage =
                historialVisitaService.obtenerHistorialPaginado(visitanteId, pageable);

        return ResponseEntity.ok(ApiResponseDTO.success(
                historialPage,
                "Historial obtenido exitosamente",
                request.getRequestURI()
        ));
    }


}