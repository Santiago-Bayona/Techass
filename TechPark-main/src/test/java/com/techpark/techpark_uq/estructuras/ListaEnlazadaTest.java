package com.techpark.techpark_uq.estructuras;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas de ListaEnlazada - Estructura propia")
class ListaEnlazadaTest {
    
    private ListaEnlazada<String> lista;
    
    @BeforeEach
    void setUp() {
        lista = new ListaEnlazada<>();
        System.out.println("✅ Configuración de prueba completada");
    }
    
    @Test
    @DisplayName("1. Debe agregar elementos al final")
    void testAgregar() {
        System.out.println("\n📝 Probando agregar elementos al final...");
        
        lista.agregar("Primero");
        lista.agregar("Segundo");
        lista.agregar("Tercero");
        
        System.out.println("   Lista actual: " + lista);
        System.out.println("   Tamaño: " + lista.getTamanio());
        System.out.println("   Elemento en posición 0: " + lista.obtener(0));
        System.out.println("   Elemento en posición 1: " + lista.obtener(1));
        System.out.println("   Elemento en posición 2: " + lista.obtener(2));
        
        assertEquals(3, lista.getTamanio());
        assertEquals("Primero", lista.obtener(0));
        assertEquals("Segundo", lista.obtener(1));
        assertEquals("Tercero", lista.obtener(2));
        
        System.out.println("✅ Agregar al final funciona correctamente");
    }
    
    @Test
    @DisplayName("2. Debe agregar elementos al inicio")
    void testAgregarAlInicio() {
        System.out.println("\n📝 Probando agregar elementos al inicio...");
        
        lista.agregarAlInicio("Segundo");
        lista.agregarAlInicio("Primero");
        
        System.out.println("   Lista actual: " + lista);
        System.out.println("   Tamaño: " + lista.getTamanio());
        
        assertEquals(2, lista.getTamanio());
        assertEquals("Primero", lista.obtener(0));
        assertEquals("Segundo", lista.obtener(1));
        
        System.out.println("✅ Agregar al inicio funciona correctamente");
    }
    
    @Test
    @DisplayName("3. Debe eliminar elementos por valor")
    void testEliminarPorValor() {
        System.out.println("\n📝 Probando eliminar elementos por valor...");
        
        lista.agregar("A");
        lista.agregar("B");
        lista.agregar("C");
        
        System.out.println("   Lista antes de eliminar: " + lista);
        
        assertTrue(lista.eliminar("B"));
        
        System.out.println("   Lista después de eliminar 'B': " + lista);
        System.out.println("   Tamaño: " + lista.getTamanio());
        
        assertEquals(2, lista.getTamanio());
        assertEquals("A", lista.obtener(0));
        assertEquals("C", lista.obtener(1));
        
        assertFalse(lista.eliminar("X"));
        System.out.println("   Intentar eliminar 'X' (no existe): false");
        
        System.out.println("✅ Eliminar por valor funciona correctamente");
    }
    
    @Test
    @DisplayName("4. Debe eliminar elementos por posición")
    void testEliminarPorPosicion() {
        System.out.println("\n📝 Probando eliminar elementos por posición...");
        
        lista.agregar("A");
        lista.agregar("B");
        lista.agregar("C");
        
        System.out.println("   Lista antes de eliminar: " + lista);
        
        String eliminado = lista.eliminarEnPosicion(1);
        
        System.out.println("   Elemento eliminado en posición 1: " + eliminado);
        System.out.println("   Lista después de eliminar: " + lista);
        System.out.println("   Tamaño: " + lista.getTamanio());
        
        assertEquals("B", eliminado);
        assertEquals(2, lista.getTamanio());
        assertEquals("A", lista.obtener(0));
        assertEquals("C", lista.obtener(1));
        
        System.out.println("✅ Eliminar por posición funciona correctamente");
    }
    
    @Test
    @DisplayName("5. Debe buscar la posición de un elemento")
    void testBuscar() {
        System.out.println("\n📝 Probando buscar posición de elementos...");
        
        lista.agregar("A");
        lista.agregar("B");
        lista.agregar("C");
        
        System.out.println("   Lista: " + lista);
        System.out.println("   Posición de 'A': " + lista.buscar("A"));
        System.out.println("   Posición de 'B': " + lista.buscar("B"));
        System.out.println("   Posición de 'C': " + lista.buscar("C"));
        System.out.println("   Posición de 'X' (no existe): " + lista.buscar("X"));
        
        assertEquals(0, lista.buscar("A"));
        assertEquals(1, lista.buscar("B"));
        assertEquals(2, lista.buscar("C"));
        assertEquals(-1, lista.buscar("X"));
        
        System.out.println("✅ Búsqueda de posición funciona correctamente");
    }
    
    @Test
    @DisplayName("6. Debe verificar si contiene un elemento")
    void testContiene() {
        System.out.println("\n📝 Probando verificar si contiene elementos...");
        
        lista.agregar("A");
        lista.agregar("B");
        
        System.out.println("   Lista: " + lista);
        System.out.println("   ¿Contiene 'A'? " + lista.contiene("A"));
        System.out.println("   ¿Contiene 'B'? " + lista.contiene("B"));
        System.out.println("   ¿Contiene 'C'? " + lista.contiene("C"));
        
        assertTrue(lista.contiene("A"));
        assertTrue(lista.contiene("B"));
        assertFalse(lista.contiene("C"));
        
        System.out.println("✅ Verificar contenido funciona correctamente");
    }
    
    @Test
    @DisplayName("7. Debe revertir la lista correctamente")
    void testRevertir() {
        System.out.println("\n📝 Probando revertir la lista...");
        
        lista.agregar("A");
        lista.agregar("B");
        lista.agregar("C");
        lista.agregar("D");
        
        System.out.println("   Lista original: " + lista);
        
        lista.revertir();
        
        System.out.println("   Lista revertida: " + lista);
        
        assertEquals("D", lista.obtener(0));
        assertEquals("C", lista.obtener(1));
        assertEquals("B", lista.obtener(2));
        assertEquals("A", lista.obtener(3));
        
        System.out.println("✅ Revertir lista funciona correctamente");
    }
    
    @Test
    @DisplayName("8. El iterador debe recorrer todos los elementos")
    void testIterador() {
        System.out.println("\n📝 Probando el iterador...");
        
        lista.agregar("A");
        lista.agregar("B");
        lista.agregar("C");
        
        System.out.print("   Recorriendo lista con iterador: ");
        StringBuilder sb = new StringBuilder();
        for (String elemento : lista) {
            sb.append(elemento);
            System.out.print(elemento + " ");
        }
        System.out.println();
        
        assertEquals("ABC", sb.toString());
        
        System.out.println("✅ Iterador funciona correctamente");
    }
}