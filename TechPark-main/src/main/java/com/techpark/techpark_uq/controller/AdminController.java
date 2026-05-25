package com.techpark.techpark_uq.controller;

import com.techpark.techpark_uq.model.dto.ApiResponseDTO;
import com.techpark.techpark_uq.service.AdminService;
import com.techpark.techpark_uq.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMINISTRADOR')")  // Solo administradores
public class AdminController {

    private final AdminService adminService;
    private final EmailService emailService;

    @DeleteMapping("/usuario/{usuarioId}/{adminId}")
    public ResponseEntity<ApiResponseDTO<Void>> eliminarUsuario(
            @PathVariable Long usuarioId,
            @PathVariable Long adminId,
            HttpServletRequest request) {
        
        adminService.eliminarUsuarioPermanente(usuarioId, adminId);
        
        return ResponseEntity.ok(ApiResponseDTO.success(
            null,
            "Usuario eliminado exitosamente",
            request.getRequestURI()
        ));
    }

    @PutMapping("/usuario/desactivar/{usuarioId}/{adminId}")
    public ResponseEntity<ApiResponseDTO<Void>> desactivarUsuario(
            @PathVariable Long usuarioId,
            @PathVariable Long adminId,
            HttpServletRequest request) {
        
        adminService.desactivarUsuario(usuarioId, adminId);
        
        return ResponseEntity.ok(ApiResponseDTO.success(
            null,
            "Usuario desactivado exitosamente",
            request.getRequestURI()
        ));
    }

    @PutMapping("/usuario/reactivar/{usuarioId}")
    public ResponseEntity<ApiResponseDTO<Void>> reactivarUsuario(
            @PathVariable Long usuarioId,
            HttpServletRequest request) {
        
        adminService.reactivarUsuario(usuarioId);
        
        return ResponseEntity.ok(ApiResponseDTO.success(
            null,
            "Usuario reactivado exitosamente",
            request.getRequestURI()
        ));
    }

    @PostMapping("/promocion/enviar")
    public ResponseEntity<ApiResponseDTO<Void>> enviarPromocion(
            @RequestBody Map<String, String> datos,
            HttpServletRequest request) {
        
        String email = datos.get("email");
        String nombre = datos.get("nombre");
        String titulo = datos.get("titulo");
        String descripcion = datos.get("descripcion");
        String codigo = datos.get("codigo");
        
        emailService.enviarPromocion(email, nombre, titulo, descripcion, codigo);
        
        return ResponseEntity.ok(ApiResponseDTO.success(
            null,
            "Promoción enviada exitosamente",
            request.getRequestURI()
        ));
    }

    @PostMapping("/promocion/masiva")
    public ResponseEntity<ApiResponseDTO<Void>> enviarPromocionMasiva(
            @RequestBody Map<String, String> datos,
            @RequestParam List<String> emails,
            HttpServletRequest request) {
        
        String titulo = datos.get("titulo");
        String mensaje = datos.get("mensaje");
        
        emailService.enviarCorreoMasivo(emails, "🎢 " + titulo, titulo, mensaje);
        
        return ResponseEntity.ok(ApiResponseDTO.success(
            null,
            "Promociones enviadas exitosamente",
            request.getRequestURI()
        ));
    }
}