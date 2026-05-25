package com.techpark.techpark_uq.estructuras;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas de ColaPrioridad - Estructura propia")
class ColaPrioridadTest {
    
    private ColaPrioridad<String> colaPrioridad;
    
    @BeforeEach
    void setUp() {
        colaPrioridad = new ColaPrioridad<>();
        System.out.println("✅ Configuración de prueba completada");
    }
    
    @Test
    @DisplayName("Debe encolar y desencolar elementos correctamente")
    void testEncolarYDesencolar() {
        System.out.println("\n📝 Probando encolar y desencolar...");
        
        colaPrioridad.encolar("Visitante1", 2);
        colaPrioridad.encolar("Visitante2", 1);
        
        assertEquals(2, colaPrioridad.getTamanio());
        assertFalse(colaPrioridad.estaVacia());
        
        System.out.println("✅ Encolar y desencolar funciona correctamente");
        System.out.println("   Tamaño de cola: " + colaPrioridad.getTamanio());
    }
    
    @Test
    @DisplayName("Fast-Pass (prioridad 1) debe salir antes que General (prioridad 2)")
    void testPrioridadFastPassAntesQueGeneral() {
        System.out.println("\n🎫 Probando prioridad Fast-Pass vs General...");
        
        colaPrioridad.encolar("General_Ana", 2);
        colaPrioridad.encolar("FastPass_Juan", 1);
        colaPrioridad.encolar("General_Carlos", 2);
        colaPrioridad.encolar("FastPass_Maria", 1);
        
        String primero = colaPrioridad.desencolar();
        String segundo = colaPrioridad.desencolar();
        String tercero = colaPrioridad.desencolar();
        String cuarto = colaPrioridad.desencolar();
        
        System.out.println("   Orden de salida:");
        System.out.println("   1º: " + primero);
        System.out.println("   2º: " + segundo);
        System.out.println("   3º: " + tercero);
        System.out.println("   4º: " + cuarto);
        
        assertEquals("FastPass_Juan", primero);
        assertEquals("FastPass_Maria", segundo);
        assertEquals("General_Ana", tercero);
        assertEquals("General_Carlos", cuarto);
        
        System.out.println("✅ Fast-Pass tiene prioridad sobre General");
    }
    
    @Test
    @DisplayName("Elementos con misma prioridad deben salir en orden FIFO")
    void testMismaPrioridadMantieneOrden() {
        System.out.println("\n📋 Probando orden FIFO para misma prioridad...");
        
        colaPrioridad.encolar("General_Ana", 2);
        colaPrioridad.encolar("General_Beto", 2);
        colaPrioridad.encolar("General_Carla", 2);
        
        String primero = colaPrioridad.desencolar();
        String segundo = colaPrioridad.desencolar();
        String tercero = colaPrioridad.desencolar();
        
        System.out.println("   Orden de salida (misma prioridad):");
        System.out.println("   1º: " + primero);
        System.out.println("   2º: " + segundo);
        System.out.println("   3º: " + tercero);
        
        assertEquals("General_Ana", primero);
        assertEquals("General_Beto", segundo);
        assertEquals("General_Carla", tercero);
        
        System.out.println("✅ Se mantiene el orden FIFO");
    }
    
    @Test
    @DisplayName("VerPrimero debe mostrar el elemento sin eliminarlo")
    void testVerPrimero() {
        System.out.println("\n👀 Probando verPrimero sin eliminar...");
        
        colaPrioridad.encolar("Primero", 2);
        colaPrioridad.encolar("Segundo", 1);
        
        String primero = colaPrioridad.verPrimero();
        System.out.println("   Primer elemento (sin eliminar): " + primero);
        
        assertEquals("Segundo", primero);
        assertEquals(2, colaPrioridad.getTamanio());
        
        System.out.println("✅ verPrimero funciona correctamente");
        System.out.println("   La cola sigue teniendo " + colaPrioridad.getTamanio() + " elementos");
    }
    
    @Test
    @DisplayName("Debe lanzar excepción al desencolar cola vacía")
    void testDesencolarColaVacia() {
        System.out.println("\n⚠️ Probando excepción en cola vacía...");
        
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            colaPrioridad.desencolar();
        });
        
        System.out.println("   Excepción lanzada: " + exception.getClass().getSimpleName());
        System.out.println("   Mensaje: " + exception.getMessage());
        
        System.out.println("✅ Se lanza excepción correctamente");
    }
    
    @Test
    @DisplayName("Debe vaciar la cola correctamente")
    void testVaciarCola() {
        System.out.println("\n🧹 Probando vaciar cola...");
        
        colaPrioridad.encolar("Elemento1", 1);
        colaPrioridad.encolar("Elemento2", 2);
        
        System.out.println("   Antes de vaciar - Tamaño: " + colaPrioridad.getTamanio());
        
        colaPrioridad.vaciar();
        
        System.out.println("   Después de vaciar - Tamaño: " + colaPrioridad.getTamanio());
        
        assertEquals(0, colaPrioridad.getTamanio());
        assertTrue(colaPrioridad.estaVacia());
        
        System.out.println("✅ Cola vaciada correctamente");
    }
}