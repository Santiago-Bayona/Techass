package com.techpark.techpark_uq.controller;

import com.techpark.techpark_uq.model.dto.ApiResponseDTO;
import com.techpark.techpark_uq.model.dto.ForgotPasswordRequestDTO;
import com.techpark.techpark_uq.model.dto.LoginRequestDTO;
import com.techpark.techpark_uq.model.dto.RegistroVisitanteRequest;
import com.techpark.techpark_uq.model.dto.ResetPasswordRequestDTO;
import com.techpark.techpark_uq.model.dto.UsuarioDTO;
import com.techpark.techpark_uq.security.JwtTokenProvider;
import com.techpark.techpark_uq.service.AuthService;
import com.techpark.techpark_uq.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UsuarioService usuarioService;
    private final AuthService authService;

    /**
     * Iniciar sesión
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> login(
            @Valid @RequestBody LoginRequestDTO loginRequest,
            HttpServletRequest request) {
        
        log.info("🔑 Intento de login para email: {}", loginRequest.getEmail());
        
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(),
                    loginRequest.getPassword()
                )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = tokenProvider.generarToken(authentication);
            
            UsuarioDTO usuario = usuarioService.obtenerUsuarioPorEmail(loginRequest.getEmail());
            
            Map<String, Object> response = new HashMap<>();
            response.put("token", jwt);
            response.put("id", usuario.getId());
            response.put("nombre", usuario.getNombre());
            response.put("email", usuario.getEmail());
            response.put("rol", usuario.getRol());
            
            // ⭐ AGREGAR CAMPOS ADICIONALES
            response.put("documento", usuario.getDocumento() != null ? usuario.getDocumento() : "");
            response.put("edad", usuario.getEdad() != null ? usuario.getEdad() : 0);
            response.put("estatura", usuario.getEstatura() != null ? usuario.getEstatura() : 0.0);

            log.info("✅ Login exitoso para usuario: {} - Documento: {}, Edad: {}, Estatura: {}", 
                     usuario.getEmail(), usuario.getDocumento(), usuario.getEdad(), usuario.getEstatura());
            
            return ResponseEntity.ok(ApiResponseDTO.success(
                response,
                "Login exitoso",
                request.getRequestURI()
            ));
            
        } catch (BadCredentialsException e) {
            log.warn("❌ Intento de login fallido para email: {}", loginRequest.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponseDTO.error(
                    "Correo electrónico o contraseña incorrectos",
                    request.getRequestURI()
                ));
        } catch (Exception e) {
            log.error("❌ Error en login: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDTO.error(
                    "Error interno del servidor",
                    request.getRequestURI()
                ));
        }
    }

    /**
     * Registrar nuevo visitante
     */
    @PostMapping("/registro")
    public ResponseEntity<ApiResponseDTO<UsuarioDTO>> registrar(
            @Valid @RequestBody RegistroVisitanteRequest registroRequest,
            HttpServletRequest request) {
        
        log.info("📝 Nuevo registro de visitante: {}", registroRequest.getEmail());
        
        UsuarioDTO nuevoUsuario = usuarioService.registrarVisitante(registroRequest);
        
        log.info("✅ Visitante registrado exitosamente: {}", nuevoUsuario.getEmail());
        
        return ResponseEntity.ok(ApiResponseDTO.success(
            nuevoUsuario,
            "Usuario registrado exitosamente",
            request.getRequestURI()
        ));
    }

    /**
     * Solicitar recuperación de contraseña
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponseDTO<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequestDTO request,
            HttpServletRequest httpRequest) {
        
        log.info("📧 Solicitud de recuperación de contraseña para: {}", request.getEmail());
        
        authService.forgotPassword(request.getEmail());
        
        return ResponseEntity.ok(ApiResponseDTO.success(
            null,
            "Si el email existe en nuestro sistema, recibirás un enlace para recuperar tu contraseña",
            httpRequest.getRequestURI()
        ));
    }

    /**
     * Restablecer contraseña con token
     */
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponseDTO<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequestDTO request,
            HttpServletRequest httpRequest) {
        
        log.info("🔐 Intentando restablecer contraseña con token");
        
        authService.resetPassword(request.getToken(), request.getNewPassword());
        
        return ResponseEntity.ok(ApiResponseDTO.success(
            null,
            "Contraseña actualizada exitosamente. Ya puedes iniciar sesión.",
            httpRequest.getRequestURI()
        ));
    }
}