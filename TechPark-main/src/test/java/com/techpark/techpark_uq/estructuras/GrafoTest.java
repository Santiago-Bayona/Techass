package com.techpark.techpark_uq.estructuras;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas de Grafo - Estructura propia con Dijkstra")
class GrafoTest {
    
    private Grafo<String> grafo;
    
    @BeforeEach
    void setUp() {
        grafo = new Grafo<>();
        System.out.println("✅ Configuración de prueba completada");
        
        // Crear mapa del parque
        grafo.agregarVertice("Entrada", "Puerta Principal", 0, 0);
        grafo.agregarVertice("MontañaRusa", "Montaña Rusa", 100, 0);
        grafo.agregarVertice("Rueda", "Rueda de Chicago", 200, 50);
        grafo.agregarVertice("CasaTerror", "Casa del Terror", 150, 100);
        grafo.agregarVertice("Tienda", "Tienda de Regalos", 50, 150);
        
        System.out.println("   Vértices creados: Entrada, MontañaRusa, Rueda, CasaTerror, Tienda");
        
        // Conectar caminos
        grafo.agregarArista("Entrada", "MontañaRusa", 100, "Camino Norte");
        grafo.agregarArista("Entrada", "Tienda", 150, "Camino Sur");
        grafo.agregarArista("MontañaRusa", "Rueda", 120, "Camino Este");
        grafo.agregarArista("MontañaRusa", "CasaTerror", 110, "Camino Sur");
        grafo.agregarArista("Rueda", "CasaTerror", 80, "Camino Oeste");
        grafo.agregarArista("CasaTerror", "Tienda", 100, "Camino Suroeste");
        
        System.out.println("   Aristas creadas: 6 caminos conectados");
    }
    
    @Test
    @DisplayName("1. Debe agregar vértices correctamente")
    void testAgregarVertices() {
        System.out.println("\n📝 Probando agregar vértices...");
        
        System.out.println("   Cantidad de vértices: " + grafo.getCantidadVertices());
        System.out.println("   ¿Contiene 'Entrada'? " + grafo.contieneVertice("Entrada"));
        System.out.println("   ¿Contiene 'MontañaRusa'? " + grafo.contieneVertice("MontañaRusa"));
        System.out.println("   ¿Contiene 'Inexistente'? " + grafo.contieneVertice("Inexistente"));
        
        assertEquals(5, grafo.getCantidadVertices());
        assertTrue(grafo.contieneVertice("Entrada"));
        assertTrue(grafo.contieneVertice("MontañaRusa"));
        assertTrue(grafo.contieneVertice("Rueda"));
        assertTrue(grafo.contieneVertice("CasaTerror"));
        assertTrue(grafo.contieneVertice("Tienda"));
        assertFalse(grafo.contieneVertice("Inexistente"));
        
        System.out.println("✅ Agregar vértices funciona correctamente");
    }
    
    @Test
    @DisplayName("2. Debe agregar aristas correctamente")
    void testAgregarAristas() {
        System.out.println("\n📝 Probando agregar aristas...");
        
        System.out.println("   Cantidad de aristas: " + grafo.getCantidadAristas());
        System.out.println("   ¿Son adyacentes Entrada y MontañaRusa? " + grafo.sonAdyacentes("Entrada", "MontañaRusa"));
        System.out.println("   ¿Son adyacentes MontañaRusa y CasaTerror? " + grafo.sonAdyacentes("MontañaRusa", "CasaTerror"));
        System.out.println("   ¿Son adyacentes Entrada y Rueda? " + grafo.sonAdyacentes("Entrada", "Rueda"));
        
        assertEquals(6, grafo.getCantidadAristas());
        assertTrue(grafo.sonAdyacentes("Entrada", "MontañaRusa"));
        assertTrue(grafo.sonAdyacentes("MontañaRusa", "CasaTerror"));
        assertFalse(grafo.sonAdyacentes("Entrada", "Rueda"));
        
        System.out.println("✅ Agregar aristas funciona correctamente");
    }
    
    @Test
    @DisplayName("3. Dijkstra debe encontrar el camino más corto")
    void testDijkstra() {
        System.out.println("\n📝 Probando algoritmo Dijkstra...");
        
        System.out.println("   Buscando ruta de 'Entrada' a 'Rueda'...");
        var resultado = grafo.dijkstra("Entrada", "Rueda");
        
        ListaEnlazada<String> camino = resultado.getCamino();
        System.out.print("   Camino encontrado: ");
        for (String nodo : camino) {
            System.out.print(nodo + " → ");
        }
        System.out.println(" FIN");
        
        System.out.println("   Distancia total: " + resultado.getDistanciaTotal() + " metros");
        System.out.println("   " + resultado.imprimirCamino());
        
        assertEquals(3, camino.getTamanio());
        assertEquals("Entrada", camino.obtener(0));
        assertEquals("MontañaRusa", camino.obtener(1));
        assertEquals("Rueda", camino.obtener(2));
        assertEquals(220.0, resultado.getDistanciaTotal(), 0.1);
        
        System.out.println("✅ Dijkstra funciona correctamente");
    }
    
    @Test
    @DisplayName("4. Debe encontrar atracciones cercanas")
    void testEncontrarMasCercanos() {
        System.out.println("\n📝 Probando encontrar atracciones cercanas...");
        
        System.out.println("   Atracciones cercanas a 'Entrada' (top 3):");
        var cercanos = grafo.encontrarMasCercanos("Entrada", 3);
        
        for (int i = 0; i < cercanos.size(); i++) {
            var entry = cercanos.get(i);
            System.out.println("   " + (i+1) + ". " + entry.getValue() + " - distancia: " + entry.getKey() + "m");
        }
        
        assertEquals(3, cercanos.size());
        assertEquals("MontañaRusa", cercanos.get(0).getValue());
        
        System.out.println("✅ Encontrar atracciones cercanas funciona correctamente");
    }
    
    @Test
    @DisplayName("5. BFS debe encontrar camino con menos aristas")
    void testBFS() {
        System.out.println("\n📝 Probando BFS (búsqueda por amplitud)...");
        
        System.out.println("   Buscando camino BFS de 'Entrada' a 'Tienda'...");
        var bfsCamino = grafo.bfs("Entrada", "Tienda");
        
        System.out.print("   Camino BFS encontrado: ");
        for (String nodo : bfsCamino) {
            System.out.print(nodo + " → ");
        }
        System.out.println(" FIN");
        System.out.println("   Número de aristas: " + (bfsCamino.getTamanio() - 1));
        
        assertEquals(2, bfsCamino.getTamanio());
        assertEquals("Entrada", bfsCamino.obtener(0));
        assertEquals("Tienda", bfsCamino.obtener(1));
        
        System.out.println("✅ BFS funciona correctamente");
    }
    
    @Test
    @DisplayName("6. Debe verificar adyacencia correctamente")
    void testSonAdyacentes() {
        System.out.println("\n📝 Probando verificar adyacencia...");
        
        System.out.println("   ¿Entrada y MontañaRusa son adyacentes? " + grafo.sonAdyacentes("Entrada", "MontañaRusa"));
        System.out.println("   ¿Entrada y Tienda son adyacentes? " + grafo.sonAdyacentes("Entrada", "Tienda"));
        System.out.println("   ¿Entrada y Rueda son adyacentes? " + grafo.sonAdyacentes("Entrada", "Rueda"));
        
        assertTrue(grafo.sonAdyacentes("Entrada", "MontañaRusa"));
        assertTrue(grafo.sonAdyacentes("Entrada", "Tienda"));
        assertFalse(grafo.sonAdyacentes("Entrada", "Rueda"));
        
        System.out.println("✅ Verificar adyacencia funciona correctamente");
    }
    
    @Test
    @DisplayName("7. Debe obtener el peso de una arista")
    void testObtenerPeso() {
        System.out.println("\n📝 Probando obtener peso de aristas...");
        
        double peso1 = grafo.obtenerPeso("Entrada", "MontañaRusa");
        double peso2 = grafo.obtenerPeso("MontañaRusa", "CasaTerror");
        double peso3 = grafo.obtenerPeso("Entrada", "Rueda");
        
        System.out.println("   Peso Entrada → MontañaRusa: " + peso1 + "m");
        System.out.println("   Peso MontañaRusa → CasaTerror: " + peso2 + "m");
        System.out.println("   Peso Entrada → Rueda (no conectada): " + peso3);
        
        assertEquals(100.0, peso1, 0.1);
        assertEquals(110.0, peso2, 0.1);
        assertEquals(-1.0, peso3, 0.1);
        
        System.out.println("✅ Obtener peso de aristas funciona correctamente");
    }
    
    @Test
    @DisplayName("8. Debe obtener el grado de un vértice")
    void testObtenerGradoVertice() {
        System.out.println("\n📝 Probando obtener grado de vértices...");
        
        int gradoEntrada = grafo.obtenerGradoVertice("Entrada");
        int gradoMontañaRusa = grafo.obtenerGradoVertice("MontañaRusa");
        int gradoRueda = grafo.obtenerGradoVertice("Rueda");
        
        System.out.println("   Grado de Entrada: " + gradoEntrada);
        System.out.println("   Grado de MontañaRusa: " + gradoMontañaRusa);
        System.out.println("   Grado de Rueda: " + gradoRueda);
        
        assertEquals(2, gradoEntrada);
        assertEquals(3, gradoMontañaRusa);
        assertEquals(2, gradoRueda);
        
        System.out.println("✅ Obtener grado de vértices funciona correctamente");
    }
    
    @Test
    @DisplayName("9. Debe detectar clusters correctamente")
    void testDetectarClusters() {
        System.out.println("\n📝 Probando detección de clusters...");
        
        var clusters = grafo.detectarClusters(100.0);
        
        System.out.println("   Número de clusters detectados: " + clusters.size());
        for (int i = 0; i < clusters.size(); i++) {
            System.out.print("   Cluster " + (i+1) + ": ");
            for (String nodo : clusters.get(i)) {
                System.out.print(nodo + " ");
            }
            System.out.println();
        }
        
        assertNotNull(clusters);
        
        System.out.println("✅ Detección de clusters funciona correctamente");
    }
    
    @Test
    @DisplayName("10. Debe verificar si el grafo es conexo")
    void testEsConexo() {
        System.out.println("\n📝 Probando verificar si el grafo es conexo...");
        
        boolean conexo = grafo.esConexo();
        System.out.println("   ¿El grafo es conexo? " + conexo);
        
        assertTrue(conexo);
        
        System.out.println("✅ Verificar conectividad funciona correctamente");
    }
}