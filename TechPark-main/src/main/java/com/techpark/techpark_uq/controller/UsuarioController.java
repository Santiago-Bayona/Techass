package com.techpark.techpark_uq.controller;

import com.techpark.techpark_uq.model.dto.ApiResponseDTO;
import com.techpark.techpark_uq.model.dto.RecargaSaldoDTO;
import com.techpark.techpark_uq.model.dto.UsuarioDTO;
import com.techpark.techpark_uq.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UsuarioController {

    private final UsuarioService usuarioService;


    /**
     * Obtener perfil de usuario por ID
     */
    @GetMapping("/perfil/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponseDTO<UsuarioDTO>> obtenerPerfil(
            @PathVariable Long id,
            HttpServletRequest request) {
        
        UsuarioDTO usuario = usuarioService.obtenerUsuarioPorId(id);
        
        return ResponseEntity.ok(ApiResponseDTO.success(
            usuario,
            "Perfil obtenido exitosamente",
            request.getRequestURI()
        ));
    }

    /**
     * Obtener perfil por email
     */
    @GetMapping("/perfil/email/{email}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponseDTO<UsuarioDTO>> obtenerPerfilPorEmail(
            @PathVariable String email,
            HttpServletRequest request) {
        
        UsuarioDTO usuario = usuarioService.obtenerUsuarioPorEmail(email);
        
        return ResponseEntity.ok(ApiResponseDTO.success(
            usuario,
            "Perfil obtenido exitosamente",
            request.getRequestURI()
        ));
    }

    /**
     * Recargar saldo virtual (para visitantes)
     */
    @PostMapping("/recargar")
    @PreAuthorize("hasRole('VISITANTE')")
    public ResponseEntity<ApiResponseDTO<UsuarioDTO>> recargarSaldo(
            @Valid @RequestBody RecargaSaldoDTO recarga,
            HttpServletRequest request) {
        
        log.info("💰 Recarga de saldo solicitada - Usuario: {}, Monto: ${}", 
                 recarga.getUsuarioId(), recarga.getMonto());
        
        UsuarioDTO usuario = usuarioService.recargarSaldo(recarga.getUsuarioId(), recarga.getMonto());
        
        String mensaje = String.format("Recarga exitosa. Nuevo saldo: $%.2f", 
                                       usuario.getSaldoVirtual());
        
        return ResponseEntity.ok(ApiResponseDTO.success(
            usuario,
            mensaje,
            request.getRequestURI()
        ));
    }

    /**
     * Consultar saldo actual
     */
    @GetMapping("/saldo/{id}")
    @PreAuthorize("hasRole('VISITANTE')")
    public ResponseEntity<ApiResponseDTO<Double>> consultarSaldo(
            @PathVariable Long id,
            HttpServletRequest request) {
        
        UsuarioDTO usuario = usuarioService.obtenerUsuarioPorId(id);
        Double saldo = usuario.getSaldoVirtual() != null ? usuario.getSaldoVirtual() : 0.0;
        
        return ResponseEntity.ok(ApiResponseDTO.success(
            saldo,
            "Saldo consultado exitosamente",
            request.getRequestURI()
        ));
    }

    /**
     * Actualizar perfil de usuario
     */
    @PutMapping("/perfil/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponseDTO<UsuarioDTO>> actualizarPerfil(
            @PathVariable Long id,
            @Valid @RequestBody com.techpark.techpark_uq.model.dto.RegistroVisitanteRequest requestPerfil,
            HttpServletRequest request) {
        
        UsuarioDTO usuario = usuarioService.actualizarPerfil(id, requestPerfil);
        
        return ResponseEntity.ok(ApiResponseDTO.success(
            usuario,
            "Perfil actualizado exitosamente",
            request.getRequestURI()
        ));
    }

    /**
     * Cambiar contraseña
     */
    @PostMapping("/cambiar-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponseDTO<Void>> cambiarPassword(
            @RequestParam Long id,
            @RequestParam String passwordActual,
            @RequestParam String passwordNueva,
            HttpServletRequest request) {
        
        usuarioService.cambiarPassword(id, passwordActual, passwordNueva);
        
        return ResponseEntity.ok(ApiResponseDTO.success(
            null,
            "Contraseña cambiada exitosamente",
            request.getRequestURI()
        ));
    }

    @GetMapping("")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponseDTO<List<UsuarioDTO>>> listarTodos(
            HttpServletRequest request) {

        log.info("📋 Listando todos los usuarios");
        List<UsuarioDTO> usuarios = usuarioService.listarTodos();

        return ResponseEntity.ok(ApiResponseDTO.success(
                usuarios,
                "Usuarios obtenidos exitosamente",
                request.getRequestURI()
        ));
    }

    /**
     * Desactivar usuario (ADMIN)
     */
    @PutMapping("/desactivar/{usuarioId}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponseDTO<Void>> desactivarUsuario(
            @PathVariable Long usuarioId,
            HttpServletRequest request) {

        log.info("❌ Admin desactivando usuario ID: {}", usuarioId);
        usuarioService.desactivarUsuario(usuarioId);

        return ResponseEntity.ok(ApiResponseDTO.success(
                null,
                "Usuario desactivado exitosamente",
                request.getRequestURI()
        ));
    }

    /**
     * Reactivar usuario (ADMIN)
     */
    @PutMapping("/reactivar/{usuarioId}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponseDTO<Void>> reactivarUsuario(
            @PathVariable Long usuarioId,
            HttpServletRequest request) {

        log.info("✅ Admin reactivando usuario ID: {}", usuarioId);
        usuarioService.reactivarUsuario(usuarioId);

        return ResponseEntity.ok(ApiResponseDTO.success(
                null,
                "Usuario reactivado exitosamente",
                request.getRequestURI()
        ));
    }

    /**
     * Eliminar usuario permanentemente (ADMIN)
     */
    @DeleteMapping("/{usuarioId}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponseDTO<Void>> eliminarUsuario(
            @PathVariable Long usuarioId,
            HttpServletRequest request) {

        log.info("🗑️ Admin eliminando usuario ID: {}", usuarioId);
        usuarioService.eliminarUsuario(usuarioId);

        return ResponseEntity.ok(ApiResponseDTO.success(
                null,
                "Usuario eliminado exitosamente",
                request.getRequestURI()
        ));
    }
}