package com.techpark.techpark_uq.service;

import com.techpark.techpark_uq.exception.BusinessException;
import com.techpark.techpark_uq.model.dto.RegistroVisitanteRequest;
import com.techpark.techpark_uq.model.dto.UsuarioDTO;
import com.techpark.techpark_uq.model.entity.*;
import com.techpark.techpark_uq.repository.UsuarioRepository;
import com.techpark.techpark_uq.mapper.UsuarioMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas de UsuarioService - Registro y autenticación")
class UsuarioServiceTest {
    
    @Mock
    private UsuarioRepository usuarioRepository;
    
    @Mock
    private UsuarioMapper usuarioMapper;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @InjectMocks
    private UsuarioService usuarioService;
    
    private RegistroVisitanteRequest registroRequest;
    private Visitante visitante;
    private UsuarioDTO usuarioDTO;
    
    @BeforeEach
    void setUp() {
        System.out.println("✅ Configuración de prueba completada");
        
        registroRequest = new RegistroVisitanteRequest();
        registroRequest.setNombre("Carlos Test");
        registroRequest.setDocumento("12345678");
        registroRequest.setEmail("carlos@test.com");
        registroRequest.setPassword("password123");
        registroRequest.setEdad(25);
        registroRequest.setEstatura(1.75);
        registroRequest.setTipoTicket("GENERAL");
        
        visitante = new Visitante();
        visitante.setId(1L);
        visitante.setNombre("Carlos Test");
        visitante.setDocumento("12345678");
        visitante.setEmail("carlos@test.com");
        visitante.setEdad(25);
        visitante.setEstatura(1.75);
        visitante.setTicketActivo("GENERAL");
        visitante.setRol(RolUsuario.VISITANTE);
        
        usuarioDTO = new UsuarioDTO();
        usuarioDTO.setId(1L);
        usuarioDTO.setNombre("Carlos Test");
        usuarioDTO.setDocumento("12345678");
        usuarioDTO.setEmail("carlos@test.com");
        usuarioDTO.setEdad(25);
        usuarioDTO.setEstatura(1.75);
        usuarioDTO.setRol("VISITANTE");
        
        System.out.println("   Usuario de prueba: " + registroRequest.getNombre());
        System.out.println("   Email: " + registroRequest.getEmail());
    }
    
    @Test
    @DisplayName("1. Debe registrar un nuevo visitante correctamente")
    void testRegistrarVisitante() {
        System.out.println("\n📝 Probando registro de nuevo visitante...");
        
        when(usuarioRepository.existsByEmail(registroRequest.getEmail())).thenReturn(false);
        when(usuarioRepository.existsByDocumento(registroRequest.getDocumento())).thenReturn(false);
        when(usuarioMapper.toEntity(registroRequest)).thenReturn(visitante);
        when(passwordEncoder.encode(registroRequest.getPassword())).thenReturn("encodedPassword");
        when(usuarioRepository.save(any(Visitante.class))).thenReturn(visitante);
        when(usuarioMapper.toDto(visitante)).thenReturn(usuarioDTO);
        
        UsuarioDTO resultado = usuarioService.registrarVisitante(registroRequest);
        
        System.out.println("   Visitante registrado: " + resultado.getNombre());
        System.out.println("   ID asignado: " + resultado.getId());
        System.out.println("   Email: " + resultado.getEmail());
        
        assertNotNull(resultado);
        assertEquals("Carlos Test", resultado.getNombre());
        assertEquals("carlos@test.com", resultado.getEmail());
        
        verify(usuarioRepository, times(1)).save(any(Visitante.class));
        
        System.out.println("✅ Registro de visitante funciona correctamente");
    }
    
    @Test
    @DisplayName("2. No debe registrar si el email ya existe")
    void testRegistrarConEmailExistente() {
        System.out.println("\n📝 Probando registro con email existente...");
        
        when(usuarioRepository.existsByEmail(registroRequest.getEmail())).thenReturn(true);
        
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            usuarioService.registrarVisitante(registroRequest);
        });
        
        System.out.println("   Email existente: " + registroRequest.getEmail());
        System.out.println("   Excepción lanzada: " + exception.getMessage());
        
        assertTrue(exception.getMessage().contains("email"));
        assertEquals("EMAIL_EXISTENTE", exception.getErrorCode());
        
        verify(usuarioRepository, never()).save(any(Visitante.class));
        
        System.out.println("✅ Validación de email duplicado funciona correctamente");
    }
    
    @Test
    @DisplayName("3. No debe registrar si el documento ya existe")
    void testRegistrarConDocumentoExistente() {
        System.out.println("\n📝 Probando registro con documento existente...");
        
        when(usuarioRepository.existsByEmail(registroRequest.getEmail())).thenReturn(false);
        when(usuarioRepository.existsByDocumento(registroRequest.getDocumento())).thenReturn(true);
        
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            usuarioService.registrarVisitante(registroRequest);
        });
        
        System.out.println("   Documento existente: " + registroRequest.getDocumento());
        System.out.println("   Excepción lanzada: " + exception.getMessage());
        
        assertTrue(exception.getMessage().contains("documento"));
        assertEquals("DOCUMENTO_EXISTENTE", exception.getErrorCode());
        
        verify(usuarioRepository, never()).save(any(Visitante.class));
        
        System.out.println("✅ Validación de documento duplicado funciona correctamente");
    }
    
    @Test
    @DisplayName("4. Debe obtener un usuario por ID correctamente")
    void testObtenerUsuarioPorId() {
        System.out.println("\n📝 Probando obtener usuario por ID...");
        
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(visitante));
        when(usuarioMapper.toDto(visitante)).thenReturn(usuarioDTO);
        
        UsuarioDTO resultado = usuarioService.obtenerUsuarioPorId(1L);
        
        System.out.println("   Usuario encontrado - ID: " + resultado.getId());
        System.out.println("   Nombre: " + resultado.getNombre());
        System.out.println("   Email: " + resultado.getEmail());
        
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Carlos Test", resultado.getNombre());
        
        System.out.println("✅ Obtener usuario por ID funciona correctamente");
    }
    
    @Test
    @DisplayName("5. Debe lanzar excepción si el usuario no existe")
    void testUsuarioNoEncontrado() {
        System.out.println("\n📝 Probando búsqueda de usuario inexistente...");
        
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());
        
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            usuarioService.obtenerUsuarioPorId(99L);
        });
        
        System.out.println("   ID buscado: 99");
        System.out.println("   Excepción lanzada: " + exception.getMessage());
        
        assertTrue(exception.getMessage().contains("no encontrado"));
        assertEquals("USUARIO_NO_ENCONTRADO", exception.getErrorCode());
        
        System.out.println("✅ Validación de usuario no encontrado funciona correctamente");
    }
    
    @Test
    @DisplayName("6. Debe obtener un usuario por email correctamente")
    void testObtenerUsuarioPorEmail() {
        System.out.println("\n📝 Probando obtener usuario por email...");
        
        when(usuarioRepository.findByEmail("carlos@test.com")).thenReturn(Optional.of(visitante));
        when(usuarioMapper.toDto(visitante)).thenReturn(usuarioDTO);
        
        UsuarioDTO resultado = usuarioService.obtenerUsuarioPorEmail("carlos@test.com");
        
        System.out.println("   Usuario encontrado - Email: " + resultado.getEmail());
        System.out.println("   Nombre: " + resultado.getNombre());
        
        assertNotNull(resultado);
        assertEquals("carlos@test.com", resultado.getEmail());
        
        System.out.println("✅ Obtener usuario por email funciona correctamente");
    }
}