package com.techpark.techpark_uq.controller;

import com.techpark.techpark_uq.model.dto.AlertaClimaDTO;
import com.techpark.techpark_uq.model.dto.ApiResponseDTO;
import com.techpark.techpark_uq.model.dto.SimulacionClimaDTO;
import com.techpark.techpark_uq.model.entity.Atraccion;
import com.techpark.techpark_uq.service.ClimaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clima")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ClimaController {
    
    private final ClimaService climaService;
    
    @PostMapping("/activar/{administradorId}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponseDTO<AlertaClimaDTO>> activarAlerta(
            @Valid @RequestBody SimulacionClimaDTO simulacion,
            @PathVariable Long administradorId,
            HttpServletRequest request) {
        
        AlertaClimaDTO alerta = climaService.activarAlertaClimatica(simulacion, administradorId);
        
        return ResponseEntity.ok(ApiResponseDTO.success(
            alerta,
            "Alerta climática activada. Atracciones afectadas cerradas.",
            request.getRequestURI()
        ));
    }
    
    @PutMapping("/desactivar/{alertaId}/{administradorId}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponseDTO<AlertaClimaDTO>> desactivarAlerta(
            @PathVariable Long alertaId,
            @PathVariable Long administradorId,
            HttpServletRequest request) {
        
        AlertaClimaDTO alerta = climaService.desactivarAlertaClimatica(alertaId, administradorId);
        
        return ResponseEntity.ok(ApiResponseDTO.success(
            alerta,
            "Alerta climática desactivada. Atracciones reactivadas.",
            request.getRequestURI()
        ));
    }
    
    @GetMapping("/activas")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponseDTO<List<AlertaClimaDTO>>> obtenerAlertasActivas(
            HttpServletRequest request) {
        
        List<AlertaClimaDTO> alertas = climaService.obtenerAlertasActivas();
        
        return ResponseEntity.ok(ApiResponseDTO.success(
            alertas,
            "Alertas climáticas activas obtenidas",
            request.getRequestURI()
        ));
    }
    
    @GetMapping("/historial")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponseDTO<List<AlertaClimaDTO>>> obtenerHistorial(
            HttpServletRequest request) {
        
        List<AlertaClimaDTO> historial = climaService.obtenerHistorialAlertas();
        
        return ResponseEntity.ok(ApiResponseDTO.success(
            historial,
            "Historial de alertas obtenido",
            request.getRequestURI()
        ));
    }
    
    @GetMapping("/atracciones-cerradas")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponseDTO<List<Atraccion>>> obtenerAtraccionesCerradasPorClima(
            HttpServletRequest request) {
        
        List<Atraccion> atracciones = climaService.obtenerAtraccionesCerradasPorClima();
        
        return ResponseEntity.ok(ApiResponseDTO.success(
            atracciones,
            "Atracciones cerradas por clima obtenidas",
            request.getRequestURI()
        ));
    }
    
    @GetMapping("/hay-alerta")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponseDTO<Map<String, Boolean>>> hayAlertaActiva(
            HttpServletRequest request) {
        
        boolean hayAlerta = climaService.hayAlertaActiva();
        
        return ResponseEntity.ok(ApiResponseDTO.success(
            Map.of("hayAlerta", hayAlerta),
            "Estado de alerta obtenido",
            request.getRequestURI()
        ));
    }
}