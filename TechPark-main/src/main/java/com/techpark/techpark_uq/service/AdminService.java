package com.techpark.techpark_uq.service;

import com.techpark.techpark_uq.exception.BusinessException;
import com.techpark.techpark_uq.model.entity.Usuario;
import com.techpark.techpark_uq.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final UsuarioRepository usuarioRepository;
    private final EmailService emailService;

    /**
     * Eliminar usuario físicamente (borrado permanente)
     */
    @Transactional
    public void eliminarUsuarioPermanente(Long usuarioId, Long adminId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado", "USUARIO_NO_ENCONTRADO"));
        
        // No permitir eliminar a sí mismo
        if (usuarioId.equals(adminId)) {
            throw new BusinessException("No puedes eliminar tu propia cuenta", "AUTO_ELIMINACION_NO_PERMITIDA");
        }
        
        String email = usuario.getEmail();
        String nombre = usuario.getNombre();
        
        usuarioRepository.delete(usuario);
        log.info("Usuario eliminado permanentemente: {} ({})", nombre, email);
        
        // Opcional: Enviar correo de cuenta eliminada
        // emailService.enviarCorreoCuentaEliminada(nombre, email);
    }

    /**
     * Soft delete - marcar como inactivo (recomendado)
     */
    @Transactional
    public void desactivarUsuario(Long usuarioId, Long adminId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado", "USUARIO_NO_ENCONTRADO"));
        
        if (usuarioId.equals(adminId)) {
            throw new BusinessException("No puedes desactivar tu propia cuenta", "AUTO_DESACTIVACION_NO_PERMITIDA");
        }
        
        usuario.setActivo(false);
        usuarioRepository.save(usuario);
        log.info("Usuario desactivado: {} ({})", usuario.getNombre(), usuario.getEmail());
        
        // Enviar correo de notificación
        // emailService.enviarCorreoCuentaDesactivada(usuario.getNombre(), usuario.getEmail());
    }

    /**
     * Reactivar usuario
     */
    @Transactional
    public void reactivarUsuario(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado", "USUARIO_NO_ENCONTRADO"));
        
        usuario.setActivo(true);
        usuarioRepository.save(usuario);
        log.info("Usuario reactivado: {} ({})", usuario.getNombre(), usuario.getEmail());
        
        emailService.enviarCorreoBienvenida(usuario.getNombre(), usuario.getEmail());
    }
}