package com.techpark.techpark_uq.estructuras;

import lombok.Getter;
import java.util.*;

/**
 * Implementación propia de Árbol Binario de Búsqueda (ABB)
 * Para búsqueda rápida de atracciones por nombre o ID
 * @param <K> Tipo de la clave (debe ser Comparable)
 * @param <V> Tipo del valor (ej: Atraccion)
 */
public class ArbolBinarioBusqueda<K extends Comparable<K>, V> implements Iterable<ArbolBinarioBusqueda.Entry<K, V>> {
    
    private NodoArbol<K, V> raiz;
    private int tamanio;
    
    /**
     * Clase Entry que representa un par clave-valor
     */
    @Getter
    public static class Entry<K, V> {
        private final K key;
        private final V value;
        
        public Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Entry<?, ?> entry = (Entry<?, ?>) obj;
            return Objects.equals(key, entry.key);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(key);
        }
        
        @Override
        public String toString() {
            return key + "=" + value;
        }
    }
    
    /**
     * Clase interna Nodo del árbol
     */
    private static class NodoArbol<K, V> {
        private K key;
        private V value;
        private NodoArbol<K, V> izquierdo;
        private NodoArbol<K, V> derecho;
        
        public NodoArbol(K key, V value) {
            this.key = key;
            this.value = value;
            this.izquierdo = null;
            this.derecho = null;
        }
    }
    
    // ============= CONSTRUCTORES =============
    
    public ArbolBinarioBusqueda() {
        this.raiz = null;
        this.tamanio = 0;
    }
    
    // ============= OPERACIONES BÁSICAS =============
    
    /**
     * Insertar un par clave-valor en el árbol
     * Si la clave ya existe, actualiza el valor
     */
    public void insertar(K key, V value) {
        if (key == null) {
            throw new IllegalArgumentException("La clave no puede ser nula");
        }
        raiz = insertarRecursivo(raiz, key, value);
    }
    
    private NodoArbol<K, V> insertarRecursivo(NodoArbol<K, V> nodo, K key, V value) {
        if (nodo == null) {
            tamanio++;
            return new NodoArbol<>(key, value);
        }
        
        int comparacion = key.compareTo(nodo.key);
        
        if (comparacion < 0) {
            nodo.izquierdo = insertarRecursivo(nodo.izquierdo, key, value);
        } else if (comparacion > 0) {
            nodo.derecho = insertarRecursivo(nodo.derecho, key, value);
        } else {
            // Clave ya existe, actualizar valor
            nodo.value = value;
        }
        
        return nodo;
    }
    
    /**
     * Buscar un valor por su clave
     * @return el valor asociado o null si no existe
     */
    public V buscar(K key) {
        if (key == null) {
            return null;
        }
        NodoArbol<K, V> nodo = buscarRecursivo(raiz, key);
        return nodo != null ? nodo.value : null;
    }
    
    private NodoArbol<K, V> buscarRecursivo(NodoArbol<K, V> nodo, K key) {
        if (nodo == null) {
            return null;
        }
        
        int comparacion = key.compareTo(nodo.key);
        
        if (comparacion < 0) {
            return buscarRecursivo(nodo.izquierdo, key);
        } else if (comparacion > 0) {
            return buscarRecursivo(nodo.derecho, key);
        } else {
            return nodo;
        }
    }
    
    /**
     * Verificar si una clave existe en el árbol
     */
    public boolean contiene(K key) {
        return buscar(key) != null;
    }
    
    /**
     * Eliminar un elemento por su clave
     * @return el valor eliminado o null si no existía
     */
    public V eliminar(K key) {
        if (key == null || !contiene(key)) {
            return null;
        }
        
        V valorEliminado = buscar(key);
        raiz = eliminarRecursivo(raiz, key);
        tamanio--;
        return valorEliminado;
    }
    
    private NodoArbol<K, V> eliminarRecursivo(NodoArbol<K, V> nodo, K key) {
        if (nodo == null) {
            return null;
        }
        
        int comparacion = key.compareTo(nodo.key);
        
        if (comparacion < 0) {
            nodo.izquierdo = eliminarRecursivo(nodo.izquierdo, key);
        } else if (comparacion > 0) {
            nodo.derecho = eliminarRecursivo(nodo.derecho, key);
        } else {
            // Caso 1: Sin hijos
            if (nodo.izquierdo == null && nodo.derecho == null) {
                return null;
            }
            // Caso 2: Un solo hijo
            else if (nodo.izquierdo == null) {
                return nodo.derecho;
            }
            else if (nodo.derecho == null) {
                return nodo.izquierdo;
            }
            // Caso 3: Dos hijos - encontrar el sucesor in-order
            else {
                NodoArbol<K, V> sucesor = encontrarMinimo(nodo.derecho);
                nodo.key = sucesor.key;
                nodo.value = sucesor.value;
                nodo.derecho = eliminarRecursivo(nodo.derecho, sucesor.key);
            }
        }
        
        return nodo;
    }
    
    // ============= OPERACIONES DE MÍNIMO Y MÁXIMO =============
    
    /**
     * Encontrar el mínimo (la clave más pequeña)
     */
    public Entry<K, V> encontrarMinimo() {
        if (estaVacio()) {
            return null;
        }
        NodoArbol<K, V> minimo = encontrarMinimo(raiz);
        return new Entry<>(minimo.key, minimo.value);
    }
    
    private NodoArbol<K, V> encontrarMinimo(NodoArbol<K, V> nodo) {
        NodoArbol<K, V> actual = nodo;
        while (actual.izquierdo != null) {
            actual = actual.izquierdo;
        }
        return actual;
    }
    
    /**
     * Encontrar el máximo (la clave más grande)
     */
    public Entry<K, V> encontrarMaximo() {
        if (estaVacio()) {
            return null;
        }
        NodoArbol<K, V> maximo = encontrarMaximo(raiz);
        return new Entry<>(maximo.key, maximo.value);
    }
    
    private NodoArbol<K, V> encontrarMaximo(NodoArbol<K, V> nodo) {
        NodoArbol<K, V> actual = nodo;
        while (actual.derecho != null) {
            actual = actual.derecho;
        }
        return actual;
    }
    
    // ============= RECORRIDOS DEL ÁRBOL =============
    
    /**
     * Recorrido In-Order (izquierdo → raíz → derecho)
     * Devuelve los elementos ordenados por clave
     */
    public List<Entry<K, V>> inOrder() {
        List<Entry<K, V>> resultado = new ArrayList<>();
        inOrderRecursivo(raiz, resultado);
        return resultado;
    }
    
    private void inOrderRecursivo(NodoArbol<K, V> nodo, List<Entry<K, V>> resultado) {
        if (nodo != null) {
            inOrderRecursivo(nodo.izquierdo, resultado);
            resultado.add(new Entry<>(nodo.key, nodo.value));
            inOrderRecursivo(nodo.derecho, resultado);
        }
    }
    
    /**
     * Recorrido Pre-Order (raíz → izquierdo → derecho)
     */
    public List<Entry<K, V>> preOrder() {
        List<Entry<K, V>> resultado = new ArrayList<>();
        preOrderRecursivo(raiz, resultado);
        return resultado;
    }
    
    private void preOrderRecursivo(NodoArbol<K, V> nodo, List<Entry<K, V>> resultado) {
        if (nodo != null) {
            resultado.add(new Entry<>(nodo.key, nodo.value));
            preOrderRecursivo(nodo.izquierdo, resultado);
            preOrderRecursivo(nodo.derecho, resultado);
        }
    }
    
    /**
     * Recorrido Post-Order (izquierdo → derecho → raíz)
     */
    public List<Entry<K, V>> postOrder() {
        List<Entry<K, V>> resultado = new ArrayList<>();
        postOrderRecursivo(raiz, resultado);
        return resultado;
    }
    
    private void postOrderRecursivo(NodoArbol<K, V> nodo, List<Entry<K, V>> resultado) {
        if (nodo != null) {
            postOrderRecursivo(nodo.izquierdo, resultado);
            postOrderRecursivo(nodo.derecho, resultado);
            resultado.add(new Entry<>(nodo.key, nodo.value));
        }
    }
    
    /**
     * Recorrido por niveles (BFS)
     */
    public List<List<Entry<K, V>>> nivelOrder() {
        List<List<Entry<K, V>>> resultado = new ArrayList<>();
        if (raiz == null) {
            return resultado;
        }
        
        Queue<NodoArbol<K, V>> cola = new LinkedList<>();
        cola.offer(raiz);
        
        while (!cola.isEmpty()) {
            int nivelSize = cola.size();
            List<Entry<K, V>> nivelActual = new ArrayList<>();
            
            for (int i = 0; i < nivelSize; i++) {
                NodoArbol<K, V> nodo = cola.poll();
                nivelActual.add(new Entry<>(nodo.key, nodo.value));
                
                if (nodo.izquierdo != null) {
                    cola.offer(nodo.izquierdo);
                }
                if (nodo.derecho != null) {
                    cola.offer(nodo.derecho);
                }
            }
            resultado.add(nivelActual);
        }
        
        return resultado;
    }
    
    // ============= BÚSQUEDA POR RANGO (CORREGIDA) =============
    
    /**
     * Búsqueda por rango (claves entre k1 y k2, inclusive)
     */
    public List<Entry<K, V>> buscarPorRango(K k1, K k2) {
        if (k1 == null || k2 == null) {
            return new ArrayList<>();
        }
        
        // Asegurar que k1 <= k2
        if (k1.compareTo(k2) > 0) {
            K temp = k1;
            k1 = k2;
            k2 = temp;
        }
        
        List<Entry<K, V>> resultado = new ArrayList<>();
        buscarPorRangoRecursivo(raiz, k1, k2, resultado);
        return resultado;
    }
    
    private void buscarPorRangoRecursivo(NodoArbol<K, V> nodo, K k1, K k2, List<Entry<K, V>> resultado) {
        if (nodo == null) {
            return;
        }
        
        int compIzq = nodo.key.compareTo(k1);
        int compDer = nodo.key.compareTo(k2);
        
        // Si la clave actual es mayor que k1, explorar subárbol izquierdo
        if (compIzq > 0) {
            buscarPorRangoRecursivo(nodo.izquierdo, k1, k2, resultado);
        }
        
        // Si la clave actual está dentro del rango [k1, k2], agregarla
        if (compIzq >= 0 && compDer <= 0) {
            resultado.add(new Entry<>(nodo.key, nodo.value));
        }
        
        // Si la clave actual es menor que k2, explorar subárbol derecho
        if (compDer < 0) {
            buscarPorRangoRecursivo(nodo.derecho, k1, k2, resultado);
        }
    }
    
    // ============= PROPIEDADES DEL ÁRBOL =============
    
    public int getTamanio() {
        return tamanio;
    }
    
    public boolean estaVacio() {
        return tamanio == 0;
    }
    
    public int getAltura() {
        return calcularAltura(raiz);
    }
    
    private int calcularAltura(NodoArbol<K, V> nodo) {
        if (nodo == null) {
            return 0;
        }
        return 1 + Math.max(calcularAltura(nodo.izquierdo), calcularAltura(nodo.derecho));
    }
    
    public void vaciar() {
        raiz = null;
        tamanio = 0;
    }
    
    /**
     * Verifica si el árbol está balanceado
     */
    public boolean estaBalanceado() {
        return verificarBalanceado(raiz) != -1;
    }
    
    private int verificarBalanceado(NodoArbol<K, V> nodo) {
        if (nodo == null) {
            return 0;
        }
        
        int alturaIzq = verificarBalanceado(nodo.izquierdo);
        int alturaDer = verificarBalanceado(nodo.derecho);
        
        if (alturaIzq == -1 || alturaDer == -1 || Math.abs(alturaIzq - alturaDer) > 1) {
            return -1;
        }
        
        return Math.max(alturaIzq, alturaDer) + 1;
    }
    
    /**
     * Obtiene el predecesor in-order de una clave
     */
    public Entry<K, V> obtenerPredecesor(K key) {
        if (!contiene(key)) {
            return null;
        }
        
        NodoArbol<K, V> nodo = raiz;
        NodoArbol<K, V> predecesor = null;
        
        while (nodo != null) {
            int comparacion = key.compareTo(nodo.key);
            
            if (comparacion > 0) {
                predecesor = nodo;
                nodo = nodo.derecho;
            } else if (comparacion < 0) {
                nodo = nodo.izquierdo;
            } else {
                if (nodo.izquierdo != null) {
                    predecesor = encontrarMinimo(nodo.izquierdo);
                }
                break;
            }
        }
        
        return predecesor != null ? new Entry<>(predecesor.key, predecesor.value) : null;
    }
    
    /**
     * Obtiene el sucesor in-order de una clave
     */
    public Entry<K, V> obtenerSucesor(K key) {
        if (!contiene(key)) {
            return null;
        }
        
        NodoArbol<K, V> nodo = raiz;
        NodoArbol<K, V> sucesor = null;
        
        while (nodo != null) {
            int comparacion = key.compareTo(nodo.key);
            
            if (comparacion < 0) {
                sucesor = nodo;
                nodo = nodo.izquierdo;
            } else if (comparacion > 0) {
                nodo = nodo.derecho;
            } else {
                if (nodo.derecho != null) {
                    sucesor = encontrarMinimo(nodo.derecho);
                }
                break;
            }
        }
        
        return sucesor != null ? new Entry<>(sucesor.key, sucesor.value) : null;
    }
    
    // ============= MÉTODOS ADICIONALES =============
    
    public List<K> obtenerClaves() {
        List<K> claves = new ArrayList<>();
        for (Entry<K, V> entry : inOrder()) {
            claves.add(entry.getKey());
        }
        return claves;
    }
    
    public List<V> obtenerValores() {
        List<V> valores = new ArrayList<>();
        for (Entry<K, V> entry : inOrder()) {
            valores.add(entry.getValue());
        }
        return valores;
    }
    
    public boolean esBSTValido() {
        return esBSTValidoRecursivo(raiz, null, null);
    }
    
    private boolean esBSTValidoRecursivo(NodoArbol<K, V> nodo, K min, K max) {
        if (nodo == null) {
            return true;
        }
        
        if ((min != null && nodo.key.compareTo(min) <= 0) ||
            (max != null && nodo.key.compareTo(max) >= 0)) {
            return false;
        }
        
        return esBSTValidoRecursivo(nodo.izquierdo, min, nodo.key) &&
               esBSTValidoRecursivo(nodo.derecho, nodo.key, max);
    }
    
    // ============= ITERADOR =============
    
    @Override
    public Iterator<Entry<K, V>> iterator() {
        return inOrder().iterator();
    }
    
    // ============= REPRESENTACIÓN VISUAL =============
    
    public String imprimirArbolVisual() {
        StringBuilder sb = new StringBuilder();
        imprimirArbolVisualRecursivo(raiz, 0, sb);
        return sb.toString();
    }
    
    private void imprimirArbolVisualRecursivo(NodoArbol<K, V> nodo, int nivel, StringBuilder sb) {
        if (nodo == null) {
            return;
        }
        
        imprimirArbolVisualRecursivo(nodo.derecho, nivel + 1, sb);
        
        for (int i = 0; i < nivel; i++) {
            sb.append("    ");
        }
        sb.append("└── ").append(nodo.key).append(" = ").append(nodo.value).append("\n");
        
        imprimirArbolVisualRecursivo(nodo.izquierdo, nivel + 1, sb);
    }
    
    @Override
    public String toString() {
        return "ArbolBinarioBusqueda{" +
               "tamaño=" + tamanio +
               ", altura=" + getAltura() +
               ", balanceado=" + estaBalanceado() +
               ", elementos=" + inOrder() +
               "}";
    }
}