package com.techpark.techpark_uq.service;


import com.techpark.techpark_uq.model.entity.*;
import com.techpark.techpark_uq.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas de MantenimientoService")
class MantenimientoServiceTest {
    
    @Mock
    private AtraccionRepository atraccionRepository;
    
    @Mock
    private MantenimientoRepository mantenimientoRepository;
    
    @Mock
    private UsuarioRepository usuarioRepository;
    
    @InjectMocks
    private MantenimientoService mantenimientoService;
    
    private Atraccion atraccion;
    
    @BeforeEach
    void setUp() {
        System.out.println("✅ Configuración de prueba completada");
        
        atraccion = new Atraccion();
        atraccion.setId(1L);
        atraccion.setNombre("Montaña Rusa");
        atraccion.setContadorVisitantes(450);
        atraccion.setEstado(EstadoAtraccion.ACTIVA);
        atraccion.setCapacidadMaxima(20);
        
        System.out.println("   Atracción: " + atraccion.getNombre());
        System.out.println("   Visitantes iniciales: " + atraccion.getContadorVisitantes());
    }
    
    @Test
    @DisplayName("1. Atracción debe bloquearse al alcanzar 500 visitantes")
    void testBloqueoAlAlcanzar500() {
        System.out.println("\n📝 Probando bloqueo al alcanzar 500 visitantes...");
        
        atraccion.setContadorVisitantes(500);
        System.out.println("   Visitantes actuales: " + atraccion.getContadorVisitantes());
        
        when(atraccionRepository.findById(1L)).thenReturn(Optional.of(atraccion));
        when(atraccionRepository.save(any(Atraccion.class))).thenReturn(atraccion);
        when(mantenimientoRepository.save(any(Mantenimiento.class))).thenAnswer(i -> i.getArgument(0));
        
        mantenimientoService.verificarMantenimiento(1L);
        
        System.out.println("   Estado después de verificación: " + atraccion.getEstado());
        System.out.println("   Motivo de cierre: " + atraccion.getMotivoCierre());
        
        assertEquals(EstadoAtraccion.MANTENIMIENTO, atraccion.getEstado());
        assertNotNull(atraccion.getMotivoCierre());
        assertTrue(atraccion.getMotivoCierre().contains("500"));
        
        System.out.println("✅ Bloqueo al alcanzar 500 funciona correctamente");
    }
    
    @Test
    @DisplayName("2. No debe bloquearse con menos de 500 visitantes")
    void testNoBloqueoAntesDe500() {
        System.out.println("\n📝 Probando que NO hay bloqueo antes de 500...");
        
        atraccion.setContadorVisitantes(400);
        System.out.println("   Visitantes actuales: " + atraccion.getContadorVisitantes());
        
        when(atraccionRepository.findById(1L)).thenReturn(Optional.of(atraccion));
        
        mantenimientoService.verificarMantenimiento(1L);
        
        System.out.println("   Estado después de verificación: " + atraccion.getEstado());
        
        assertEquals(EstadoAtraccion.ACTIVA, atraccion.getEstado());
        
        System.out.println("✅ No hay bloqueo antes de 500 funciona correctamente");
    }
    
    @Test
    @DisplayName("3. Debe generar alerta temprana a los 450 visitantes")
    void testAlertaTemprana() {
        System.out.println("\n📝 Probando alerta temprana a 450 visitantes...");
        
        atraccion.setContadorVisitantes(450);
        System.out.println("   Visitantes actuales: " + atraccion.getContadorVisitantes());
        
        when(atraccionRepository.findById(1L)).thenReturn(Optional.of(atraccion));
        when(mantenimientoRepository.save(any(Mantenimiento.class))).thenAnswer(i -> i.getArgument(0));
        
        mantenimientoService.verificarMantenimiento(1L);
        
        verify(mantenimientoRepository, atLeastOnce()).save(any(Mantenimiento.class));
        
        System.out.println("✅ Alerta temprana generada correctamente");
    }
}