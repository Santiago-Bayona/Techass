package com.techpark.techpark_uq.service;

import com.techpark.techpark_uq.exception.BusinessException;
import com.techpark.techpark_uq.model.dto.RegistroVisitanteRequest;
import com.techpark.techpark_uq.model.dto.UsuarioDTO;
import com.techpark.techpark_uq.model.entity.RolUsuario;
import com.techpark.techpark_uq.model.entity.Usuario;
import com.techpark.techpark_uq.model.entity.Visitante;
import com.techpark.techpark_uq.repository.ColaVirtualRepository;
import com.techpark.techpark_uq.repository.UsuarioRepository;
import com.techpark.techpark_uq.mapper.UsuarioMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final ColaVirtualRepository colaVirtualRepository;

    /**
     * Registrar un nuevo visitante con envío de correo de bienvenida
     */
    @Transactional
    public UsuarioDTO registrarVisitante(RegistroVisitanteRequest request) {
        log.info("📝 Registrando nuevo visitante: {}", request.getEmail());
        
        // Validar email único
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("El email ya está registrado", "EMAIL_EXISTENTE");
        }

        // Validar documento único
        if (usuarioRepository.existsByDocumento(request.getDocumento())) {
            throw new BusinessException("El documento ya está registrado", "DOCUMENTO_EXISTENTE");
        }

        // Validar edad mínima (opcional)
        if (request.getEdad() < 0 || request.getEdad() > 120) {
            throw new BusinessException("Edad no válida", "EDAD_INVALIDA");
        }

        // Validar estatura
        if (request.getEstatura() < 0.5 || request.getEstatura() > 2.5) {
            throw new BusinessException("Estatura no válida", "ESTATURA_INVALIDA");
        }

        // Crear visitante
        Visitante visitante = new Visitante();
        visitante.setNombre(request.getNombre());
        visitante.setDocumento(request.getDocumento());
        visitante.setEmail(request.getEmail());
        visitante.setPassword(passwordEncoder.encode(request.getPassword()));
        visitante.setEdad(request.getEdad());
        visitante.setEstatura(request.getEstatura());
        visitante.setRol(RolUsuario.VISITANTE);
        visitante.setActivo(true);
        visitante.setSaldoVirtual(0.0);
        visitante.setTicketActivo(request.getTipoTicket());
        visitante.setUbicacionActual("Entrada Principal");

        Visitante saved = usuarioRepository.save(visitante);
        log.info("✅ Visitante registrado exitosamente con ID: {}", saved.getId());

        // Enviar correo de bienvenida (no bloqueante - si falla, no impide el registro)
        try {
            emailService.enviarCorreoBienvenida(saved.getNombre(), saved.getEmail());
            log.info("📧 Correo de bienvenida enviado a: {}", saved.getEmail());
        } catch (Exception e) {
            log.warn("⚠️ No se pudo enviar el correo de bienvenida a {}: {}", saved.getEmail(), e.getMessage());
        }

        return usuarioMapper.toDto(saved);
    }

    /**
     * Obtener usuario por ID
     */
    public UsuarioDTO obtenerUsuarioPorId(Long id) {
        log.debug("🔍 Buscando usuario por ID: {}", id);
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado", "USUARIO_NO_ENCONTRADO"));
        return usuarioMapper.toDto(usuario);
    }

    /**
     * Obtener usuario por email
     */
    public UsuarioDTO obtenerUsuarioPorEmail(String email) {
        log.debug("🔍 Buscando usuario por email: {}", email);
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado", "USUARIO_NO_ENCONTRADO"));
        return usuarioMapper.toDto(usuario);
    }

    /**
     * Listar todos los usuarios
     */
    public List<UsuarioDTO> listarTodos() {
        log.debug("📋 Listando todos los usuarios");
        return usuarioRepository.findAll().stream()
                .map(usuarioMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Verificar si existe un email
     */
    public boolean existePorEmail(String email) {
        return usuarioRepository.existsByEmail(email);
    }

    /**
     * Obtener entidad Usuario por email (para Spring Security)
     */
    public Usuario obtenerUsuarioEntityPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado", "USUARIO_NO_ENCONTRADO"));
    }

    /**
     * Obtener todos los emails de visitantes (para envío masivo)
     */
    public List<String> obtenerEmailsDeVisitantes() {
        return usuarioRepository.findAll().stream()
                .filter(u -> u.getRol() == RolUsuario.VISITANTE)
                .filter(Usuario::getActivo)
                .map(Usuario::getEmail)
                .collect(Collectors.toList());
    }

    /**
     * Obtener todos los usuarios activos
     */
    public List<UsuarioDTO> listarUsuariosActivos() {
        return usuarioRepository.findAll().stream()
                .filter(Usuario::getActivo)
                .map(usuarioMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Obtener todos los usuarios inactivos
     */
    public List<UsuarioDTO> listarUsuariosInactivos() {
        return usuarioRepository.findAll().stream()
                .filter(u -> !u.getActivo())
                .map(usuarioMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Actualizar perfil de usuario
     */
    @Transactional
    public UsuarioDTO actualizarPerfil(Long id, RegistroVisitanteRequest request) {
        log.info("✏️ Actualizando perfil del usuario ID: {}", id);
        
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado", "USUARIO_NO_ENCONTRADO"));
        
        // Actualizar campos permitidos
        if (request.getNombre() != null) {
            usuario.setNombre(request.getNombre());
        }
        if (request.getEdad() != null) {
            usuario.setEdad(request.getEdad());
        }
        if (request.getEstatura() != null) {
            usuario.setEstatura(request.getEstatura());
        }
        if (request.getTipoTicket() != null) {
            if (usuario instanceof Visitante) {
                ((Visitante) usuario).setTicketActivo(request.getTipoTicket());
            }
        }
        
        Usuario updated = usuarioRepository.save(usuario);
        log.info("✅ Perfil actualizado para usuario: {}", updated.getEmail());
        
        return usuarioMapper.toDto(updated);
    }

    /**
     * Cambiar contraseña de usuario
     */
    @Transactional
    public void cambiarPassword(Long id, String passwordActual, String passwordNueva) {
        log.info("🔐 Cambiando contraseña para usuario ID: {}", id);
        
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado", "USUARIO_NO_ENCONTRADO"));
        
        // Verificar contraseña actual
        if (!passwordEncoder.matches(passwordActual, usuario.getPassword())) {
            throw new BusinessException("Contraseña actual incorrecta", "PASSWORD_INCORRECTA");
        }
        
        // Validar nueva contraseña
        if (passwordNueva.length() < 6) {
            throw new BusinessException("La nueva contraseña debe tener al menos 6 caracteres", "PASSWORD_CORTA");
        }
        
        usuario.setPassword(passwordEncoder.encode(passwordNueva));
        usuarioRepository.save(usuario);
        
        log.info("✅ Contraseña actualizada para usuario: {}", usuario.getEmail());
        
        try {
        } catch (Exception e) {
            log.warn("No se pudo enviar correo de cambio de contraseña");
        }
    }

    /**
     * Recargar saldo virtual de un visitante
     */
    @Transactional
    public UsuarioDTO recargarSaldo(Long id, Double monto) {
        log.info("💰 Recargando saldo para usuario ID: {}", id);
        
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado", "USUARIO_NO_ENCONTRADO"));
        
        if (usuario.getRol() != RolUsuario.VISITANTE) {
            throw new BusinessException("Solo los visitantes pueden tener saldo virtual", "ROL_INVALIDO");
        }
        
        if (monto <= 0) {
            throw new BusinessException("El monto debe ser mayor a cero", "MONTO_INVALIDO");
        }
        
        Visitante visitante = (Visitante) usuario;
        Double nuevoSaldo = (visitante.getSaldoVirtual() != null ? visitante.getSaldoVirtual() : 0.0) + monto;
        visitante.setSaldoVirtual(nuevoSaldo);
        
        Usuario updated = usuarioRepository.save(visitante);
        log.info("✅ Saldo recargado. Nuevo saldo: ${}", nuevoSaldo);
        
        return usuarioMapper.toDto(updated);
    }

    /**
     * Desactivar usuario
     */
    @Transactional
    public void desactivarUsuario(Long usuarioId) {
        log.info("❌ Desactivando usuario ID: {}", usuarioId);

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado", "USUARIO_NO_ENCONTRADO"));

        usuario.setActivo(false);
        usuarioRepository.save(usuario);

        log.info("✅ Usuario desactivado: {}", usuario.getEmail());
    }

    /**
     * Reactivar usuario
     */
    @Transactional
    public void reactivarUsuario(Long usuarioId) {
        log.info("✅ Reactivando usuario ID: {}", usuarioId);

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado", "USUARIO_NO_ENCONTRADO"));

        usuario.setActivo(true);
        usuarioRepository.save(usuario);

        log.info("✅ Usuario reactivado: {}", usuario.getEmail());
    }

    /**
     * Eliminar usuario permanentemente (elimina datos relacionados primero)
     */
    /**
     * Eliminar usuario permanentemente (elimina datos relacionados primero)
     */
    @Transactional
    public void eliminarUsuario(Long id) {
        log.info("🗑️ Eliminando usuario ID: {}", id);

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado", "USUARIO_NO_ENCONTRADO"));

        // Si es visitante, eliminar datos relacionados primero
        if (usuario instanceof Visitante) {
            Visitante visitante = (Visitante) usuario;
            log.info("🗑️ Eliminando datos relacionados del visitante: {}", visitante.getEmail());

            // Eliminar colas virtuales PRIMERO
            colaVirtualRepository.deleteByVisitanteId(id);
            log.info("   ✅ Colas virtuales eliminadas");

            // El historial se elimina en cascada automáticamente
        }

        usuarioRepository.deleteById(id);
        log.info("✅ Usuario eliminado: {}", usuario.getEmail());
    }
}