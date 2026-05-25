package com.techpark.techpark_uq.estructuras;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas de Conjunto - Estructura propia")
class ConjuntoTest {
    
    private Conjunto<String> conjunto;
    
    @BeforeEach
    void setUp() {
        conjunto = new Conjunto<>();
        System.out.println("✅ Configuración de prueba completada");
    }
    
    @Test
    @DisplayName("1. Debe agregar elementos sin duplicados")
    void testAgregarSinDuplicados() {
        System.out.println("\n📝 Probando agregar elementos sin duplicados...");
        
        assertTrue(conjunto.agregar("A"));
        System.out.println("   Agregado 'A': true");
        
        assertTrue(conjunto.agregar("B"));
        System.out.println("   Agregado 'B': true");
        
        assertFalse(conjunto.agregar("A"));
        System.out.println("   Agregar 'A' nuevamente (debería ser false): false");
        
        System.out.println("   Tamaño del conjunto: " + conjunto.getTamanio());
        
        assertEquals(2, conjunto.getTamanio());
        System.out.println("✅ Agregar sin duplicados funciona correctamente");
    }
    
    @Test
    @DisplayName("2. Debe verificar si contiene un elemento")
    void testContiene() {
        System.out.println("\n📝 Probando verificar si contiene un elemento...");
        
        conjunto.agregar("A");
        conjunto.agregar("B");
        
        System.out.println("   Conjunto contiene 'A'? " + conjunto.contiene("A"));
        System.out.println("   Conjunto contiene 'B'? " + conjunto.contiene("B"));
        System.out.println("   Conjunto contiene 'C'? " + conjunto.contiene("C"));
        
        assertTrue(conjunto.contiene("A"));
        assertTrue(conjunto.contiene("B"));
        assertFalse(conjunto.contiene("C"));
        
        System.out.println("✅ Verificar contenido funciona correctamente");
    }
    
    @Test
    @DisplayName("3. Debe eliminar elementos")
    void testEliminar() {
        System.out.println("\n📝 Probando eliminar elementos...");
        
        conjunto.agregar("A");
        conjunto.agregar("B");
        conjunto.agregar("C");
        
        System.out.println("   Conjunto antes de eliminar: tamaño=" + conjunto.getTamanio());
        
        assertTrue(conjunto.eliminar("B"));
        System.out.println("   Eliminado 'B': true");
        System.out.println("   Tamaño después de eliminar: " + conjunto.getTamanio());
        
        assertFalse(conjunto.contiene("B"));
        System.out.println("   ¿Contiene 'B'? false");
        
        assertFalse(conjunto.eliminar("X"));
        System.out.println("   Eliminar 'X' (no existe): false");
        
        assertEquals(2, conjunto.getTamanio());
        System.out.println("✅ Eliminar elementos funciona correctamente");
    }
    
    @Test
    @DisplayName("4. Debe calcular la unión de dos conjuntos")
    void testUnion() {
        System.out.println("\n📝 Probando unión de dos conjuntos...");
        
        Conjunto<String> otro = new Conjunto<>();
        
        conjunto.agregar("A");
        conjunto.agregar("B");
        System.out.println("   Conjunto 1: [A, B]");
        
        otro.agregar("B");
        otro.agregar("C");
        otro.agregar("D");
        System.out.println("   Conjunto 2: [B, C, D]");
        
        Conjunto<String> union = conjunto.union(otro);
        
        System.out.print("   Unión: ");
        for (String elem : union) {
            System.out.print(elem + " ");
        }
        System.out.println();
        System.out.println("   Tamaño de la unión: " + union.getTamanio());
        
        assertEquals(4, union.getTamanio());
        assertTrue(union.contiene("A"));
        assertTrue(union.contiene("B"));
        assertTrue(union.contiene("C"));
        assertTrue(union.contiene("D"));
        
        System.out.println("✅ Unión funciona correctamente");
    }
    
    @Test
    @DisplayName("5. Debe calcular la intersección de dos conjuntos")
    void testInterseccion() {
        System.out.println("\n📝 Probando intersección de dos conjuntos...");
        
        conjunto.agregar("A");
        conjunto.agregar("B");
        conjunto.agregar("C");
        System.out.println("   Conjunto 1: [A, B, C]");
        
        Conjunto<String> otro = new Conjunto<>();
        otro.agregar("B");
        otro.agregar("C");
        otro.agregar("D");
        System.out.println("   Conjunto 2: [B, C, D]");
        
        Conjunto<String> interseccion = conjunto.interseccion(otro);
        
        System.out.print("   Intersección: ");
        for (String elem : interseccion) {
            System.out.print(elem + " ");
        }
        System.out.println();
        System.out.println("   Tamaño de la intersección: " + interseccion.getTamanio());
        
        assertEquals(2, interseccion.getTamanio());
        assertTrue(interseccion.contiene("B"));
        assertTrue(interseccion.contiene("C"));
        assertFalse(interseccion.contiene("A"));
        assertFalse(interseccion.contiene("D"));
        
        System.out.println("✅ Intersección funciona correctamente");
    }
    
    @Test
    @DisplayName("6. Debe calcular la diferencia de dos conjuntos")
    void testDiferencia() {
        System.out.println("\n📝 Probando diferencia de dos conjuntos...");
        
        conjunto.agregar("A");
        conjunto.agregar("B");
        conjunto.agregar("C");
        System.out.println("   Conjunto 1: [A, B, C]");
        
        Conjunto<String> otro = new Conjunto<>();
        otro.agregar("B");
        otro.agregar("D");
        System.out.println("   Conjunto 2: [B, D]");
        
        Conjunto<String> diferencia = conjunto.diferencia(otro);
        
        System.out.print("   Diferencia (C1 - C2): ");
        for (String elem : diferencia) {
            System.out.print(elem + " ");
        }
        System.out.println();
        System.out.println("   Tamaño de la diferencia: " + diferencia.getTamanio());
        
        assertEquals(2, diferencia.getTamanio());
        assertTrue(diferencia.contiene("A"));
        assertTrue(diferencia.contiene("C"));
        assertFalse(diferencia.contiene("B"));
        
        System.out.println("✅ Diferencia funciona correctamente");
    }
    
    @Test
    @DisplayName("7. Debe verificar si es subconjunto")
    void testSubconjunto() {
        System.out.println("\n📝 Probando verificación de subconjunto...");
        
        conjunto.agregar("A");
        conjunto.agregar("B");
        System.out.println("   Conjunto 1: [A, B]");
        
        Conjunto<String> otro = new Conjunto<>();
        otro.agregar("A");
        otro.agregar("B");
        otro.agregar("C");
        System.out.println("   Conjunto 2: [A, B, C]");
        
        boolean esSub = conjunto.esSubconjunto(otro);
        System.out.println("   ¿C1 es subconjunto de C2? " + esSub);
        assertTrue(esSub);
        
        boolean esSubInverso = otro.esSubconjunto(conjunto);
        System.out.println("   ¿C2 es subconjunto de C1? " + esSubInverso);
        assertFalse(esSubInverso);
        
        System.out.println("✅ Verificación de subconjunto funciona correctamente");
    }
    
    @Test
    @DisplayName("8. El iterador debe recorrer todos los elementos")
    void testIterador() {
        System.out.println("\n📝 Probando el iterador...");
        
        conjunto.agregar("A");
        conjunto.agregar("B");
        conjunto.agregar("C");
        
        System.out.print("   Recorriendo conjunto con iterador: ");
        StringBuilder sb = new StringBuilder();
        for (String elemento : conjunto) {
            sb.append(elemento);
            System.out.print(elemento + " ");
        }
        System.out.println();
        
        assertEquals(3, sb.length());
        
        System.out.println("✅ Iterador funciona correctamente");
    }
    
    @Test
    @DisplayName("9. Debe vaciar el conjunto correctamente")
    void testVaciar() {
        System.out.println("\n📝 Probando vaciar conjunto...");
        
        conjunto.agregar("A");
        conjunto.agregar("B");
        conjunto.agregar("C");
        
        System.out.println("   Tamaño antes de vaciar: " + conjunto.getTamanio());
        assertEquals(3, conjunto.getTamanio());
        
        conjunto.vaciar();
        
        System.out.println("   Tamaño después de vaciar: " + conjunto.getTamanio());
        assertEquals(0, conjunto.getTamanio());
        assertTrue(conjunto.estaVacio());
        
        System.out.println("✅ Vaciar conjunto funciona correctamente");
    }
}