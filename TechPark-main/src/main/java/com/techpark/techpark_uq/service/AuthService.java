package com.techpark.techpark_uq.service;

import com.techpark.techpark_uq.exception.BusinessException;
import com.techpark.techpark_uq.model.entity.PasswordResetToken;
import com.techpark.techpark_uq.model.entity.Usuario;
import com.techpark.techpark_uq.repository.PasswordResetTokenRepository;
import com.techpark.techpark_uq.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    /**
     * Solicitar recuperación de contraseña
     */
    @Transactional
    public void forgotPassword(String email) {
        log.info("📧 Solicitud de recuperación de contraseña para: {}", email);
        
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            
            // Eliminar tokens anteriores del usuario
            tokenRepository.deleteByUsuarioId(usuario.getId());
            
            // Generar nuevo token
            String token = UUID.randomUUID().toString();
            LocalDateTime expiryDate = LocalDateTime.now().plusHours(1); // 1 hora de validez
            
            PasswordResetToken resetToken = new PasswordResetToken(token, usuario, expiryDate);
            tokenRepository.save(resetToken);
            
            // Construir URL de recuperación
            String resetUrl = "http://localhost:4200/reset-password?token=" + token;
            
            // Enviar correo electrónico
            emailService.enviarCorreoRecuperacion(usuario.getEmail(), usuario.getNombre(), resetUrl);
            
            log.info("✅ Token de recuperación generado para: {}", email);
        } else {
            log.warn("⚠️ Intento de recuperación para email no registrado: {}", email);
        }
        
        // Siempre retornar éxito por seguridad (no revelar si el email existe)
    }

    /**
     * Restablecer contraseña con token
     */
    @Transactional
    public void resetPassword(String token, String newPassword) {
        log.info("🔐 Intentando restablecer contraseña con token: {}", token);
        
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new BusinessException("Token inválido o expirado", "INVALID_TOKEN"));
        
        if (resetToken.isExpired()) {
            throw new BusinessException("El token ha expirado. Solicita un nuevo enlace de recuperación", "TOKEN_EXPIRED");
        }
        
        if (resetToken.isUsed()) {
            throw new BusinessException("El token ya fue utilizado. Solicita un nuevo enlace", "TOKEN_USED");
        }
        
        Usuario usuario = resetToken.getUsuario();
        
        // Validar que la nueva contraseña sea diferente a la actual
        if (passwordEncoder.matches(newPassword, usuario.getPassword())) {
            throw new BusinessException("La nueva contraseña debe ser diferente a la actual", "SAME_PASSWORD");
        }
        
        // Actualizar contraseña
        usuario.setPassword(passwordEncoder.encode(newPassword));
        usuarioRepository.save(usuario);
        
        // Marcar token como usado
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
        
        log.info("✅ Contraseña actualizada exitosamente para usuario: {}", usuario.getEmail());
        
        // Opcional: Enviar correo de confirmación
        emailService.enviarCorreoPasswordActualizada(usuario.getEmail(), usuario.getNombre());
    }

    /**
     * Validar si un token de recuperación es válido
     */
    public boolean isValidToken(String token) {
        return tokenRepository.findByToken(token)
                .map(t -> !t.isExpired() && !t.isUsed())
                .orElse(false);
    }
}