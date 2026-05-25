package com.techpark.techpark_uq.estructuras;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas de ArbolBinarioBusqueda - Estructura propia")
class ArbolBinarioBusquedaTest {
    
    private ArbolBinarioBusqueda<Integer, String> arbol;
    
    @BeforeEach
    void setUp() {
        arbol = new ArbolBinarioBusqueda<>();
        System.out.println("✅ Configuración de prueba completada");
    }
    
    @Test
    @DisplayName("1. Debe insertar y buscar elementos correctamente")
    void testInsertarYBuscar() {
        System.out.println("\n📝 Probando insertar y buscar elementos...");
        
        arbol.insertar(100, "Atraccion A");
        arbol.insertar(50, "Atraccion B");
        arbol.insertar(150, "Atraccion C");
        arbol.insertar(75, "Atraccion D");
        
        System.out.println("   Insertados: 100→A, 50→B, 150→C, 75→D");
        System.out.println("   Buscar 100: " + arbol.buscar(100));
        System.out.println("   Buscar 50: " + arbol.buscar(50));
        System.out.println("   Buscar 150: " + arbol.buscar(150));
        System.out.println("   Buscar 75: " + arbol.buscar(75));
        System.out.println("   Buscar 999 (no existe): " + arbol.buscar(999));
        
        assertEquals("Atraccion A", arbol.buscar(100));
        assertEquals("Atraccion B", arbol.buscar(50));
        assertEquals("Atraccion C", arbol.buscar(150));
        assertEquals("Atraccion D", arbol.buscar(75));
        assertNull(arbol.buscar(999));
        
        System.out.println("✅ Insertar y buscar funciona correctamente");
    }
    
    @Test
    @DisplayName("2. Debe verificar si contiene una clave")
    void testContiene() {
        System.out.println("\n📝 Probando verificar si contiene una clave...");
        
        arbol.insertar(100, "A");
        arbol.insertar(50, "B");
        
        System.out.println("   Insertados: 100→A, 50→B");
        System.out.println("   ¿Contiene 100? " + arbol.contiene(100));
        System.out.println("   ¿Contiene 50? " + arbol.contiene(50));
        System.out.println("   ¿Contiene 200? " + arbol.contiene(200));
        
        assertTrue(arbol.contiene(100));
        assertTrue(arbol.contiene(50));
        assertFalse(arbol.contiene(200));
        
        System.out.println("✅ Verificar contenido funciona correctamente");
    }
    
    @Test
    @DisplayName("3. El recorrido in-order debe devolver elementos ordenados")
    void testInOrder() {
        System.out.println("\n📝 Probando recorrido in-order...");
        
        arbol.insertar(100, "C");
        arbol.insertar(50, "A");
        arbol.insertar(150, "E");
        arbol.insertar(75, "B");
        arbol.insertar(125, "D");
        
        System.out.println("   Insertados: 100, 50, 150, 75, 125");
        
        var inOrder = arbol.inOrder();
        
        System.out.print("   Recorrido in-order: ");
        for (var entry : inOrder) {
            System.out.print(entry.getKey() + " ");
        }
        System.out.println();
        
        assertEquals(5, inOrder.size());
        assertEquals(50, inOrder.get(0).getKey());
        assertEquals(75, inOrder.get(1).getKey());
        assertEquals(100, inOrder.get(2).getKey());
        assertEquals(125, inOrder.get(3).getKey());
        assertEquals(150, inOrder.get(4).getKey());
        
        System.out.println("✅ Recorrido in-order funciona correctamente");
    }
    
    @Test
    @DisplayName("4. Debe encontrar el mínimo y máximo correctamente")
    void testMinimoYMaximo() {
        System.out.println("\n📝 Probando encontrar mínimo y máximo...");
        
        arbol.insertar(100, "Centro");
        arbol.insertar(50, "Izquierdo");
        arbol.insertar(150, "Derecho");
        arbol.insertar(25, "Minimo");
        arbol.insertar(200, "Maximo");
        
        System.out.println("   Insertados: 100, 50, 150, 25, 200");
        System.out.println("   Mínimo encontrado: " + arbol.encontrarMinimo().getKey());
        System.out.println("   Máximo encontrado: " + arbol.encontrarMaximo().getKey());
        
        assertEquals(25, arbol.encontrarMinimo().getKey());
        assertEquals(200, arbol.encontrarMaximo().getKey());
        
        System.out.println("✅ Encontrar mínimo y máximo funciona correctamente");
    }
    
    @Test
    @DisplayName("5. Debe eliminar elementos correctamente")
    void testEliminar() {
        System.out.println("\n📝 Probando eliminar elementos...");
        
        arbol.insertar(100, "A");
        arbol.insertar(50, "B");
        arbol.insertar(150, "C");
        
        System.out.println("   Insertados: 100→A, 50→B, 150→C");
        System.out.println("   Tamaño antes de eliminar: " + arbol.getTamanio());
        
        String eliminado = arbol.eliminar(50);
        
        System.out.println("   Eliminado clave 50: " + eliminado);
        System.out.println("   Tamaño después de eliminar: " + arbol.getTamanio());
        System.out.println("   ¿Contiene 50? " + arbol.contiene(50));
        
        assertEquals("B", eliminado);
        assertEquals(2, arbol.getTamanio());
        assertNull(arbol.buscar(50));
        
        System.out.println("✅ Eliminar elementos funciona correctamente");
    }
    
    @Test
    @DisplayName("6. La búsqueda por rango debe funcionar correctamente")
    void testBuscarPorRango() {
        System.out.println("\n📝 Probando búsqueda por rango...");
        
        arbol.insertar(100, "A");
        arbol.insertar(50, "B");
        arbol.insertar(150, "C");
        arbol.insertar(75, "D");
        arbol.insertar(125, "E");
        arbol.insertar(175, "F");
        arbol.insertar(25, "G");
        
        System.out.println("   Insertados: 100, 50, 150, 75, 125, 175, 25");
        
        var rango = arbol.buscarPorRango(60, 140);
        
        System.out.print("   Búsqueda entre 60 y 140: ");
        for (var entry : rango) {
            System.out.print(entry.getKey() + " ");
        }
        System.out.println();
        
        // CORREGIDO: valores entre 60 y 140 son 75, 100, 125 = 3 elementos
        assertEquals(3, rango.size());
        assertEquals(75, rango.get(0).getKey());
        assertEquals(100, rango.get(1).getKey());
        assertEquals(125, rango.get(2).getKey());
        
        System.out.println("✅ Búsqueda por rango funciona correctamente");
    }
    
    @Test
    @DisplayName("7. Debe conocer la altura del árbol")
    void testAltura() {
        System.out.println("\n📝 Probando altura del árbol...");
        
        arbol.insertar(100, "Raiz");
        System.out.println("   Altura después de insertar raíz: " + arbol.getAltura());
        assertEquals(1, arbol.getAltura());
        
        arbol.insertar(50, "HijoIzq");
        System.out.println("   Altura después de insertar hijo izquierdo: " + arbol.getAltura());
        assertEquals(2, arbol.getAltura());
        
        arbol.insertar(25, "NietoIzq");
        System.out.println("   Altura después de insertar nieto izquierdo: " + arbol.getAltura());
        assertEquals(3, arbol.getAltura());
        
        arbol.insertar(150, "HijoDer");
        System.out.println("   Altura después de insertar hijo derecho: " + arbol.getAltura());
        assertEquals(3, arbol.getAltura());
        
        System.out.println("✅ Cálculo de altura funciona correctamente");
    }
    
    @Test
    @DisplayName("8. Debe obtener el tamaño correctamente")
    void testTamanio() {
        System.out.println("\n📝 Probando tamaño del árbol...");
        
        System.out.println("   Tamaño inicial: " + arbol.getTamanio());
        assertEquals(0, arbol.getTamanio());
        
        arbol.insertar(100, "A");
        arbol.insertar(50, "B");
        arbol.insertar(150, "C");
        
        System.out.println("   Después de insertar 3 elementos: " + arbol.getTamanio());
        assertEquals(3, arbol.getTamanio());
        
        arbol.eliminar(50);
        System.out.println("   Después de eliminar 1 elemento: " + arbol.getTamanio());
        assertEquals(2, arbol.getTamanio());
        
        System.out.println("✅ Tamaño del árbol funciona correctamente");
    }
}