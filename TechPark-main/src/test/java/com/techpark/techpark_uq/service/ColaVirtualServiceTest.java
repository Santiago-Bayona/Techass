package com.techpark.techpark_uq.service;

import com.techpark.techpark_uq.exception.BusinessException;
import com.techpark.techpark_uq.model.dto.SolicitudColaDTO;
import com.techpark.techpark_uq.model.entity.*;
import com.techpark.techpark_uq.repository.*;
import com.techpark.techpark_uq.service.AtraccionService;
import com.techpark.techpark_uq.service.ColaVirtualService;

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
@DisplayName("Pruebas de ColaVirtualService")
class ColaVirtualServiceTest {
    
    @Mock
    private VisitanteRepository visitanteRepository;
    
    @Mock
    private AtraccionRepository atraccionRepository;
    
    @Mock
    private ColaVirtualRepository colaVirtualRepository;
    
    @Mock
    private AtraccionService atraccionService;
    
    @InjectMocks
    private ColaVirtualService colaVirtualService;
    
    private Visitante visitanteFastPass;
    private Visitante visitanteGeneral;
    private Atraccion atraccion;
    
    @BeforeEach
    void setUp() {
        System.out.println("✅ Configuración de prueba completada");
        
        visitanteFastPass = new Visitante();
        visitanteFastPass.setId(1L);
        visitanteFastPass.setNombre("Juan FastPass");
        visitanteFastPass.setEdad(25);
        visitanteFastPass.setEstatura(1.70);
        visitanteFastPass.setTicketActivo("FAST_PASS");
        visitanteFastPass.setSaldoVirtual(100.0);
        
        visitanteGeneral = new Visitante();
        visitanteGeneral.setId(2L);
        visitanteGeneral.setNombre("Ana General");
        visitanteGeneral.setEdad(30);
        visitanteGeneral.setEstatura(1.65);
        visitanteGeneral.setTicketActivo("GENERAL");
        visitanteGeneral.setSaldoVirtual(50.0);
        
        atraccion = new Atraccion();
        atraccion.setId(1L);
        atraccion.setNombre("Montaña Rusa");
        atraccion.setEdadMinima(12);
        atraccion.setAlturaMinima(1.20);
        atraccion.setCapacidadMaxima(20);
        atraccion.setEstado(EstadoAtraccion.ACTIVA);
        atraccion.setContadorVisitantes(100);
        
        System.out.println("   Visitante FastPass: " + visitanteFastPass.getNombre());
        System.out.println("   Visitante General: " + visitanteGeneral.getNombre());
        System.out.println("   Atracción: " + atraccion.getNombre());
    }
    
    @Test
    @DisplayName("1. Fast-Pass debe tener prioridad sobre General en la cola")
    void testFastPassTienePrioridad() {
        System.out.println("\n📝 Probando prioridad Fast-Pass vs General...");
        
        when(visitanteRepository.findById(1L)).thenReturn(Optional.of(visitanteFastPass));
        when(visitanteRepository.findById(2L)).thenReturn(Optional.of(visitanteGeneral));
        when(atraccionRepository.findById(1L)).thenReturn(Optional.of(atraccion));
        when(colaVirtualRepository.save(any(ColaVirtual.class))).thenAnswer(i -> i.getArgument(0));
        
        SolicitudColaDTO solicitudFastPass = new SolicitudColaDTO(1L, 1L);
        SolicitudColaDTO solicitudGeneral = new SolicitudColaDTO(2L, 1L);
        
        System.out.println("   Uniendo Fast-Pass a la cola...");
        var respuestaFastPass = colaVirtualService.unirseACola(solicitudFastPass);
        System.out.println("   Fast-Pass - Posición: " + respuestaFastPass.getPosicion() + 
                          ", Tiene prioridad: " + respuestaFastPass.getTienePrioridad());
        
        System.out.println("   Uniendo General a la cola...");
        var respuestaGeneral = colaVirtualService.unirseACola(solicitudGeneral);
        System.out.println("   General - Posición: " + respuestaGeneral.getPosicion() + 
                          ", Tiene prioridad: " + respuestaGeneral.getTienePrioridad());
        
        assertTrue(respuestaFastPass.getTienePrioridad());
        assertTrue(respuestaFastPass.getPosicion() < respuestaGeneral.getPosicion());
        
        System.out.println("✅ Fast-Pass tiene prioridad sobre General");
    }
    
    @Test
    @DisplayName("2. Debe validar restricción de edad - menor de edad no puede acceder")
    void testValidarEdad() {
        System.out.println("\n📝 Probando validación de edad...");
        
        Visitante visitanteMenor = new Visitante();
        visitanteMenor.setId(3L);
        visitanteMenor.setNombre("Pedro Menor");
        visitanteMenor.setEdad(8);
        visitanteMenor.setEstatura(1.30);
        visitanteMenor.setTicketActivo("GENERAL");
        
        System.out.println("   Visitante: " + visitanteMenor.getNombre() + ", Edad: " + visitanteMenor.getEdad());
        System.out.println("   Atracción requiere edad mínima: " + atraccion.getEdadMinima());
        
        when(visitanteRepository.findById(3L)).thenReturn(Optional.of(visitanteMenor));
        when(atraccionRepository.findById(1L)).thenReturn(Optional.of(atraccion));
        
        SolicitudColaDTO solicitud = new SolicitudColaDTO(3L, 1L);
        
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            colaVirtualService.unirseACola(solicitud);
        });
        
        System.out.println("   Excepción lanzada: " + exception.getMessage());
        assertTrue(exception.getMessage().contains("Edad mínima"));
        
        System.out.println("✅ Validación de edad funciona correctamente");
        
        // Verificar que no se llamó a save (no es necesario, pero no da error)
        verify(colaVirtualRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("3. Debe validar restricción de estatura - persona baja no puede acceder")
    void testValidarEstatura() {
        System.out.println("\n📝 Probando validación de estatura...");
        
        Visitante visitanteBajo = new Visitante();
        visitanteBajo.setId(4L);
        visitanteBajo.setNombre("Maria Baja");
        visitanteBajo.setEdad(20);
        visitanteBajo.setEstatura(1.10);
        visitanteBajo.setTicketActivo("GENERAL");
        
        System.out.println("   Visitante: " + visitanteBajo.getNombre() + ", Estatura: " + visitanteBajo.getEstatura() + "m");
        System.out.println("   Atracción requiere estatura mínima: " + atraccion.getAlturaMinima() + "m");
        
        when(visitanteRepository.findById(4L)).thenReturn(Optional.of(visitanteBajo));
        when(atraccionRepository.findById(1L)).thenReturn(Optional.of(atraccion));
        
        SolicitudColaDTO solicitud = new SolicitudColaDTO(4L, 1L);
        
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            colaVirtualService.unirseACola(solicitud);
        });
        
        System.out.println("   Excepción lanzada: " + exception.getMessage());
        assertTrue(exception.getMessage().contains("Altura mínima"));
        
        System.out.println("✅ Validación de estatura funciona correctamente");
        
        verify(colaVirtualRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("4. No debe permitir unirse a atracción en mantenimiento")
    void testAtraccionEnMantenimiento() {
        System.out.println("\n📝 Probando atracción en mantenimiento...");
        
        atraccion.setEstado(EstadoAtraccion.MANTENIMIENTO);
        atraccion.setMotivoCierre("Mantenimiento preventivo");
        
        System.out.println("   Atracción: " + atraccion.getNombre());
        System.out.println("   Estado: " + atraccion.getEstado());
        System.out.println("   Motivo: " + atraccion.getMotivoCierre());
        
        when(visitanteRepository.findById(1L)).thenReturn(Optional.of(visitanteFastPass));
        when(atraccionRepository.findById(1L)).thenReturn(Optional.of(atraccion));
        
        SolicitudColaDTO solicitud = new SolicitudColaDTO(1L, 1L);
        
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            colaVirtualService.unirseACola(solicitud);
        });
        
        System.out.println("   Excepción lanzada: " + exception.getMessage());
        assertTrue(exception.getMessage().contains("no está disponible"));
        
        System.out.println("✅ Validación de atracción en mantenimiento funciona correctamente");
        
        verify(colaVirtualRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("5. Debe rechazar si el visitante ya está en la cola")
    void testNoUnirseDosVeces() {
        System.out.println("\n📝 Probando evitar doble registro en cola...");
        
        when(visitanteRepository.findById(1L)).thenReturn(Optional.of(visitanteFastPass));
        when(atraccionRepository.findById(1L)).thenReturn(Optional.of(atraccion));
        when(colaVirtualRepository.save(any(ColaVirtual.class))).thenAnswer(i -> i.getArgument(0));
        
        SolicitudColaDTO solicitud = new SolicitudColaDTO(1L, 1L);
        
        System.out.println("   Primer intento - uniendo a cola...");
        colaVirtualService.unirseACola(solicitud);
        System.out.println("   ✓ Unido exitosamente");
        
        System.out.println("   Segundo intento - tratando de unir nuevamente...");
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            colaVirtualService.unirseACola(solicitud);
        });
        
        System.out.println("   Excepción lanzada: " + exception.getMessage());
        assertTrue(exception.getMessage().contains("ya está en la cola"));
        
        System.out.println("✅ Validación de doble registro funciona correctamente");
    }
    
    @Test
    @DisplayName("6. Debe calcular tiempo estimado de espera correctamente")
    void testTiempoEstimadoEspera() {
        System.out.println("\n📝 Probando cálculo de tiempo estimado de espera...");
        
        // CORREGIDO: Solo los stubs necesarios
        when(visitanteRepository.findById(1L)).thenReturn(Optional.of(visitanteFastPass));
        when(atraccionRepository.findById(1L)).thenReturn(Optional.of(atraccion));
        when(colaVirtualRepository.save(any(ColaVirtual.class))).thenAnswer(i -> i.getArgument(0));
        
        SolicitudColaDTO solicitud = new SolicitudColaDTO(1L, 1L);
        var respuesta = colaVirtualService.unirseACola(solicitud);
        
        System.out.println("   Visitante Fast-Pass en cola de '" + atraccion.getNombre() + "'");
        System.out.println("   Posición: " + respuesta.getPosicion());
        System.out.println("   Tiempo estimado de espera: " + respuesta.getTiempoEstimadoEspera() + " minutos");
        System.out.println("   Personas delante: " + respuesta.getPersonasDelante());
        System.out.println("   Mensaje: " + respuesta.getMensaje());
        
        assertNotNull(respuesta.getTiempoEstimadoEspera());
        assertTrue(respuesta.getTiempoEstimadoEspera() >= 0);
        assertTrue(respuesta.getTienePrioridad());
        
        System.out.println("✅ Cálculo de tiempo estimado funciona correctamente");
    }
}