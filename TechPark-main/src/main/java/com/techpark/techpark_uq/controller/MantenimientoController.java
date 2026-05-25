package com.techpark.techpark_uq.controller;

import com.techpark.techpark_uq.model.dto.AlertaMantenimientoDTO;
import com.techpark.techpark_uq.model.dto.ApiResponseDTO;
import com.techpark.techpark_uq.model.dto.RevisionTecnicaDTO;
import com.techpark.techpark_uq.model.entity.Atraccion;
import com.techpark.techpark_uq.service.MantenimientoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mantenimiento")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MantenimientoController {
    
    private final MantenimientoService mantenimientoService;

    @PostMapping("/revision")
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMINISTRADOR')")
    public ResponseEntity<ApiResponseDTO<AlertaMantenimientoDTO>> registrarRevision(
            @Valid @RequestBody RevisionTecnicaDTO revision,
            HttpServletRequest request) {
        
        AlertaMantenimientoDTO resultado = mantenimientoService.registrarRevisionTecnica(revision);
        
        return ResponseEntity.ok(ApiResponseDTO.success(
            resultado,
            "Revisión técnica registrada exitosamente. Atracción reactivada.",
            request.getRequestURI()
        ));
    }
    
    @GetMapping("/siguiente-alerta")
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMINISTRADOR')")
    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> obtenerSiguienteAlerta(
            HttpServletRequest request) {
        
        var alerta = mantenimientoService.obtenerSiguienteAlerta();
        
        Map<String, Object> respuesta = Map.of(
            "atraccionId", alerta.getAtraccionId(),
            "atraccionNombre", alerta.getAtraccionNombre(),
            "visitantesActuales", alerta.getVisitantesActuales(),
            "prioridad", alerta.getPrioridad(),
            "fechaGeneracion", alerta.getFechaGeneracion()
        );
        
        return ResponseEntity.ok(ApiResponseDTO.success(
            respuesta,
            "Siguiente alerta prioritaria obtenida",
            request.getRequestURI()
        ));
    }
    
    @GetMapping("/alertas-pendientes")
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMINISTRADOR')")
    public ResponseEntity<ApiResponseDTO<List<AlertaMantenimientoDTO>>> obtenerAlertasPendientes(
            HttpServletRequest request) {
        
        List<AlertaMantenimientoDTO> alertas = mantenimientoService.obtenerAlertasPendientes();
        
        return ResponseEntity.ok(ApiResponseDTO.success(
            alertas,
            "Alertas pendientes obtenidas",
            request.getRequestURI()
        ));
    }
    
    @GetMapping("/estadisticas")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> obtenerEstadisticas(
            HttpServletRequest request) {
        
        Map<String, Object> estadisticas = mantenimientoService.obtenerEstadisticasMantenimiento();
        
        return ResponseEntity.ok(ApiResponseDTO.success(
            estadisticas,
            "Estadísticas de mantenimiento obtenidas",
            request.getRequestURI()
        ));
    }
    
    @GetMapping("/atracciones-en-riesgo")
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMINISTRADOR')")
    public ResponseEntity<ApiResponseDTO<List<Atraccion>>> obtenerAtraccionesEnRiesgo(
            HttpServletRequest request) {
        
        List<Atraccion> atracciones = mantenimientoService.obtenerAtraccionesEnRiesgo();
        
        return ResponseEntity.ok(ApiResponseDTO.success(
            atracciones,
            "Atracciones en riesgo obtenidas",
            request.getRequestURI()
        ));
    }
    
    @GetMapping("/historial/{atraccionId}")
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMINISTRADOR')")
    public ResponseEntity<ApiResponseDTO<List<AlertaMantenimientoDTO>>> obtenerHistorial(
            @PathVariable Long atraccionId,
            HttpServletRequest request) {
        
        List<AlertaMantenimientoDTO> historial = mantenimientoService.obtenerHistorialPorAtraccion(atraccionId);
        
        return ResponseEntity.ok(ApiResponseDTO.success(
            historial,
            "Historial obtenido",
            request.getRequestURI()
        ));
    }
}