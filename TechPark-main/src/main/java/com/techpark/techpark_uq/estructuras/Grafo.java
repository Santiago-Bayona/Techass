package com.techpark.techpark_uq.estructuras;

import lombok.Getter;
import java.util.*;

/**
 * Implementación propia de un Grafo no dirigido para representar el mapa del parque
 * @param <T> Tipo de dato de los nodos (ej: Long, String, Atraccion)
 */
@Getter
public class Grafo<T> {
    
    // Estructura interna: Mapa de nodos -> Lista de aristas (Lista Enlazada propia)
    private final Map<T, ListaEnlazada<Arista<T>>> adyacencias;
    private int cantidadVertices;
    private int cantidadAristas;
    
    public Grafo() {
        this.adyacencias = new HashMap<>();
        this.cantidadVertices = 0;
        this.cantidadAristas = 0;
    }
    
    // ============= CLASE INTERNA ARISTA =============
    @Getter
    public static class Arista<T> {
        private final T destino;
        private final double peso;  // Distancia o tiempo
        private final String nombreCamino;  // Nombre del sendero
        
        public Arista(T destino, double peso, String nombreCamino) {
            this.destino = destino;
            this.peso = peso;
            this.nombreCamino = nombreCamino;
        }
        
        @Override
        public String toString() {
            return String.format("→ %s (%.1f m)", destino, peso);
        }
    }
    
    // ============= CLASE INTERNA NODO_INFO =============
    @Getter
    public static class NodoInfo<T> {
        private final T id;
        private final String nombre;
        private final double posicionX;
        private final double posicionY;
        
        public NodoInfo(T id, String nombre, double posicionX, double posicionY) {
            this.id = id;
            this.nombre = nombre;
            this.posicionX = posicionX;
            this.posicionY = posicionY;
        }
    }
    
    // Mapa adicional para metadatos del nodo
    private final Map<T, NodoInfo<T>> infoNodos = new HashMap<>();
    
    // ============= OPERACIONES BÁSICAS =============
    
    /**
     * Agrega un vértice al grafo
     */
    public void agregarVertice(T vertice, String nombre, double x, double y) {
        if (!adyacencias.containsKey(vertice)) {
            adyacencias.put(vertice, new ListaEnlazada<>());
            infoNodos.put(vertice, new NodoInfo<>(vertice, nombre, x, y));
            cantidadVertices++;
        }
    }
    
    /**
     * Agrega una arista (camino bidireccional) entre dos vértices
     */
    public void agregarArista(T origen, T destino, double peso, String nombreCamino) {
        validarVertice(origen);
        validarVertice(destino);
        
        // Arista en ambos sentidos (grafo no dirigido)
        adyacencias.get(origen).agregar(new Arista<>(destino, peso, nombreCamino));
        adyacencias.get(destino).agregar(new Arista<>(origen, peso, nombreCamino));
        cantidadAristas++;
    }
    
    /**
     * Obtiene los vecinos de un vértice
     */
    public ListaEnlazada<Arista<T>> obtenerVecinos(T vertice) {
        validarVertice(vertice);
        return adyacencias.get(vertice);
    }
    
    /**
     * Verifica si dos vértices son adyacentes
     */
    public boolean sonAdyacentes(T origen, T destino) {
        ListaEnlazada<Arista<T>> vecinos = adyacencias.get(origen);
        if (vecinos == null) return false;
        
        for (Arista<T> arista : vecinos) {
            if (arista.getDestino().equals(destino)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Obtiene el peso de la arista entre dos vértices
     */
    public double obtenerPeso(T origen, T destino) {
        ListaEnlazada<Arista<T>> vecinos = adyacencias.get(origen);
        if (vecinos == null) return -1;
        
        for (Arista<T> arista : vecinos) {
            if (arista.getDestino().equals(destino)) {
                return arista.getPeso();
            }
        }
        return -1;
    }
    
    /**
     * Obtiene información de un nodo
     */
    public NodoInfo<T> obtenerInfoNodo(T vertice) {
        return infoNodos.get(vertice);
    }
    
    /**
     * Obtiene todos los vértices del grafo
     */
    public Set<T> obtenerVertices() {
        return new HashSet<>(adyacencias.keySet());
    }
    
    /**
     * Verifica si un vértice existe en el grafo
     */
    public boolean contieneVertice(T vertice) {
        return adyacencias.containsKey(vertice);
    }
    
    /**
     * Obtiene el número de vértices
     */
    public int getCantidadVertices() {
        return cantidadVertices;
    }
    
    /**
     * Obtiene el número de aristas
     */
    public int getCantidadAristas() {
        return cantidadAristas;
    }
    
    // ============= ALGORITMOS DE BÚSQUEDA =============
    
    /**
     * BFS (Breadth-First Search) - Encuentra camino más corto en número de aristas
     * Útil para explorar el parque por niveles
     */
    public ListaEnlazada<T> bfs(T inicio, T destino) {
        validarVertice(inicio);
        validarVertice(destino);
        
        Map<T, T> predecesor = new HashMap<>();
        Set<T> visitados = new HashSet<>();
        Queue<T> cola = new LinkedList<>();
        
        visitados.add(inicio);
        cola.offer(inicio);
        
        while (!cola.isEmpty()) {
            T actual = cola.poll();
            
            if (actual.equals(destino)) {
                return reconstruirCamino(predecesor, inicio, destino);
            }
            
            ListaEnlazada<Arista<T>> vecinos = adyacencias.get(actual);
            for (Arista<T> arista : vecinos) {
                T vecino = arista.getDestino();
                if (!visitados.contains(vecino)) {
                    visitados.add(vecino);
                    predecesor.put(vecino, actual);
                    cola.offer(vecino);
                }
            }
        }
        
        return new ListaEnlazada<>();  // No hay camino
    }
    
    /**
     * ALGORITMO DE DIJKSTRA - Camino más corto por peso (distancia)
     * Este es el algoritmo más importante para el sistema
     */
    public ResultadoDijkstra<T> dijkstra(T inicio, T destino) {
        validarVertice(inicio);
        
        // Inicialización
        Map<T, Double> distancias = new HashMap<>();
        Map<T, T> predecesores = new HashMap<>();
        Set<T> noVisitados = new HashSet<>(adyacencias.keySet());
        
        // Distancia infinita para todos excepto inicio
        for (T vertice : adyacencias.keySet()) {
            distancias.put(vertice, Double.POSITIVE_INFINITY);
        }
        distancias.put(inicio, 0.0);
        
        while (!noVisitados.isEmpty()) {
            // Encontrar vértice no visitado con menor distancia
            T actual = null;
            double menorDistancia = Double.POSITIVE_INFINITY;
            
            for (T vertice : noVisitados) {
                double distancia = distancias.get(vertice);
                if (distancia < menorDistancia) {
                    menorDistancia = distancia;
                    actual = vertice;
                }
            }
            
            if (actual == null || (destino != null && actual.equals(destino))) {
                break;
            }
            
            noVisitados.remove(actual);
            
            // Actualizar distancias a vecinos
            ListaEnlazada<Arista<T>> vecinos = adyacencias.get(actual);
            for (Arista<T> arista : vecinos) {
                T vecino = arista.getDestino();
                double nuevaDistancia = distancias.get(actual) + arista.getPeso();
                
                if (nuevaDistancia < distancias.get(vecino)) {
                    distancias.put(vecino, nuevaDistancia);
                    predecesores.put(vecino, actual);
                }
            }
        }
        
        // Construir resultado
        ResultadoDijkstra<T> resultado = new ResultadoDijkstra<>();
        resultado.distancias = distancias;
        resultado.predecesores = predecesores;
        
        if (destino != null) {
            resultado.camino = reconstruirCamino(predecesores, inicio, destino);
            Double distanciaTotal = distancias.get(destino);
            resultado.distanciaTotal = (distanciaTotal != null && !distanciaTotal.isInfinite()) 
                ? distanciaTotal : -1.0;
        }
        
        return resultado;
    }
    
    /**
     * Obtiene la distancia más corta entre dos vértices
     */
    public double obtenerDistanciaMinima(T inicio, T destino) {
        ResultadoDijkstra<T> resultado = dijkstra(inicio, destino);
        return resultado.getDistanciaTotal() != null ? resultado.getDistanciaTotal() : -1.0;
    }
    
    /**
     * Encuentra las atracciones más cercanas a una ubicación (K vecinos más cercanos)
     */
    public List<ArbolBinarioBusqueda.Entry<Double, T>> encontrarMasCercanos(T origen, int k) {
        if (k <= 0) {
            return new ArrayList<>();
        }
        
        Map<T, Double> distancias = dijkstra(origen, null).getDistancias();
        
        // Usar ABB para ordenar por distancia
        ArbolBinarioBusqueda<Double, T> arbolDistancias = new ArbolBinarioBusqueda<>();
        
        for (Map.Entry<T, Double> entry : distancias.entrySet()) {
            T vertice = entry.getKey();
            Double distancia = entry.getValue();
            if (!vertice.equals(origen) && distancia != null && !distancia.isInfinite()) {
                arbolDistancias.insertar(distancia, vertice);
            }
        }
        
        // Obtener los k más cercanos
        List<ArbolBinarioBusqueda.Entry<Double, T>> masCercanos = new ArrayList<>();
        Iterator<ArbolBinarioBusqueda.Entry<Double, T>> iterator = arbolDistancias.iterator();
        int count = 0;
        
        while (iterator.hasNext() && count < k) {
            masCercanos.add(iterator.next());
            count++;
        }
        
        return masCercanos;
    }
    
    /**
     * Detecta clusters de atracciones usando DFS
     * Un cluster es un grupo de atracciones conectadas por caminos cortos
     */
    public List<ListaEnlazada<T>> detectarClusters(double pesoMaximoCluster) {
        Set<T> visitados = new HashSet<>();
        List<ListaEnlazada<T>> clusters = new ArrayList<>();
        
        for (T vertice : adyacencias.keySet()) {
            if (!visitados.contains(vertice)) {
                ListaEnlazada<T> cluster = new ListaEnlazada<>();
                dfsCluster(vertice, visitados, cluster, pesoMaximoCluster);
                if (cluster.getTamanio() >= 1) {
                    clusters.add(cluster);
                }
            }
        }
        
        return clusters;
    }
    
    /**
     * DFS recursivo para detectar clusters
     */
    private void dfsCluster(T actual, Set<T> visitados, ListaEnlazada<T> cluster, double pesoMaximoCluster) {
        visitados.add(actual);
        cluster.agregar(actual);
        
        ListaEnlazada<Arista<T>> vecinos = adyacencias.get(actual);
        for (Arista<T> arista : vecinos) {
            T vecino = arista.getDestino();
            
            // Si la distancia es pequeña, pertenece al mismo cluster
            if (!visitados.contains(vecino) && arista.getPeso() <= pesoMaximoCluster) {
                dfsCluster(vecino, visitados, cluster, pesoMaximoCluster);
            }
        }
    }
    
    /**
     * Encuentra todos los caminos entre dos vértices (hasta un límite)
     */
    public List<ListaEnlazada<T>> encontrarTodosLosCaminos(T inicio, T destino, int limiteMaximo) {
        List<ListaEnlazada<T>> todosLosCaminos = new ArrayList<>();
        Set<T> visitados = new HashSet<>();
        ListaEnlazada<T> caminoActual = new ListaEnlazada<>();
        caminoActual.agregar(inicio);
        
        dfsCaminos(inicio, destino, visitados, caminoActual, todosLosCaminos, limiteMaximo);
        return todosLosCaminos;
    }
    
    /**
     * DFS para encontrar todos los caminos
     */
    private void dfsCaminos(T actual, T destino, Set<T> visitados, 
                            ListaEnlazada<T> caminoActual, 
                            List<ListaEnlazada<T>> todosLosCaminos, 
                            int limiteMaximo) {
        
        if (actual.equals(destino)) {
            ListaEnlazada<T> copiaCamino = new ListaEnlazada<>();
            for (T nodo : caminoActual) {
                copiaCamino.agregar(nodo);
            }
            todosLosCaminos.add(copiaCamino);
            return;
        }
        
        if (caminoActual.getTamanio() >= limiteMaximo) {
            return;
        }
        
        visitados.add(actual);
        
        ListaEnlazada<Arista<T>> vecinos = adyacencias.get(actual);
        for (Arista<T> arista : vecinos) {
            T vecino = arista.getDestino();
            if (!visitados.contains(vecino)) {
                caminoActual.agregar(vecino);
                dfsCaminos(vecino, destino, visitados, caminoActual, todosLosCaminos, limiteMaximo);
                caminoActual.eliminar(vecino);
            }
        }
        
        visitados.remove(actual);
    }
    
    // ============= MÉTODOS DE UTILIDAD =============
    
    /**
     * Valida que un vértice exista en el grafo
     */
    private void validarVertice(T vertice) {
        if (!adyacencias.containsKey(vertice)) {
            throw new IllegalArgumentException("Vértice no existe en el grafo: " + vertice);
        }
    }
    
    /**
     * Reconstruye el camino desde el mapa de predecesores
     */
    private ListaEnlazada<T> reconstruirCamino(Map<T, T> predecesores, T inicio, T destino) {
        ListaEnlazada<T> camino = new ListaEnlazada<>();
        T actual = destino;
        
        while (actual != null && !actual.equals(inicio)) {
            camino.agregarAlInicio(actual);
            actual = predecesores.get(actual);
        }
        
        if (actual != null && actual.equals(inicio)) {
            camino.agregarAlInicio(inicio);
        }
        
        return camino;
    }
    
    /**
     * Calcula el grado de un vértice (número de conexiones)
     */
    public int obtenerGradoVertice(T vertice) {
        validarVertice(vertice);
        return adyacencias.get(vertice).getTamanio();
    }
    
    /**
     * Verifica si el grafo es conexo
     */
    public boolean esConexo() {
        if (cantidadVertices == 0) return true;
        
        Set<T> visitados = new HashSet<>();
        T primerVertice = adyacencias.keySet().iterator().next();
        
        dfsConexo(primerVertice, visitados);
        
        return visitados.size() == cantidadVertices;
    }
    
    /**
     * DFS para verificar conectividad
     */
    private void dfsConexo(T actual, Set<T> visitados) {
        visitados.add(actual);
        
        ListaEnlazada<Arista<T>> vecinos = adyacencias.get(actual);
        for (Arista<T> arista : vecinos) {
            T vecino = arista.getDestino();
            if (!visitados.contains(vecino)) {
                dfsConexo(vecino, visitados);
            }
        }
    }
    
    /**
     * Clase para almacenar resultados de Dijkstra
     */
    @Getter
    public static class ResultadoDijkstra<T> {
        private Map<T, Double> distancias;
        private Map<T, T> predecesores;
        private ListaEnlazada<T> camino;
        private Double distanciaTotal;
        
        /**
         * Imprime el camino encontrado de forma legible
         */
        public String imprimirCamino() {
            if (camino == null || camino.getTamanio() == 0) {
                return "No hay camino disponible";
            }
            
            StringBuilder sb = new StringBuilder();
            for (T nodo : camino) {
                sb.append(nodo);
                sb.append(" → ");
            }
            // Eliminar la última flecha
            if (sb.length() > 3) {
                sb.setLength(sb.length() - 3);
            }
            
            if (distanciaTotal != null && distanciaTotal >= 0) {
                sb.append(String.format(" | Distancia total: %.1f m", distanciaTotal));
            } else {
                sb.append(" | No se pudo alcanzar el destino");
            }
            
            return sb.toString();
        }
        
        /**
         * Obtiene la distancia total (valor seguro)
         */
        public double getDistanciaTotalSegura() {
            return distanciaTotal != null && !distanciaTotal.isInfinite() ? distanciaTotal : -1.0;
        }
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("=== MAPA DEL PARQUE ===\n");
        sb.append(String.format("Vértices: %d, Aristas: %d\n\n", cantidadVertices, cantidadAristas));
        
        for (Map.Entry<T, ListaEnlazada<Arista<T>>> entry : adyacencias.entrySet()) {
            NodoInfo<T> info = infoNodos.get(entry.getKey());
            String nombreNodo = (info != null && info.getNombre() != null) 
                ? info.getNombre() 
                : String.valueOf(entry.getKey());
            
            sb.append(String.format("📍 %s [%s]\n", nombreNodo, entry.getKey()));
            
            ListaEnlazada<Arista<T>> aristas = entry.getValue();
            
            if (aristas.estaVacia()) {
                sb.append("   └── Sin conexiones\n");
            } else {
                int contador = 0;
                for (Arista<T> arista : aristas) {
                    NodoInfo<T> infoDestino = infoNodos.get(arista.getDestino());
                    String nombreDestino = (infoDestino != null && infoDestino.getNombre() != null) 
                        ? infoDestino.getNombre() 
                        : String.valueOf(arista.getDestino());
                    
                    String prefijo = (contador == aristas.getTamanio() - 1) ? "   └── → " : "   ├── → ";
                    sb.append(String.format("%s%s (%.1f m) - %s\n", 
                        prefijo,
                        nombreDestino,
                        arista.getPeso(),
                        arista.getNombreCamino()));
                    contador++;
                }
            }
            sb.append("\n");
        }
        
        return sb.toString();
    }
}