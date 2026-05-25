package com.techpark.techpark_uq.estructuras;


import java.util.ArrayList;
import java.util.List;

/**
 * Implementación propia de Cola de Prioridad usando Heap (Montículo Binario)
 * Prioridad 1 = mayor prioridad (Fast-Pass)
 * Prioridad 2 = menor prioridad (General)
 * @param <T> Tipo de dato de los elementos
 */
public class ColaPrioridad<T> {
    
    // Heap: array donde índice 1 es la raíz (para facilitar cálculos)
    private List<NodoPrioridad<T>> heap;
    private int tamanio;
    
    private static class NodoPrioridad<T> {
        private final T dato;
        private final int prioridad;  // 1 = más alta, números más altos = menor prioridad
        private final long timestamp;  // Para desempatar por orden de llegada
        
        public NodoPrioridad(T dato, int prioridad) {
            this.dato = dato;
            this.prioridad = prioridad;
            this.timestamp = System.nanoTime();
        }
        
        public T getDato() {
            return dato;
        }
        
        public int getPrioridad() {
            return prioridad;
        }
        
        // Comparador: retorna true si este nodo tiene mayor prioridad que el otro
        public boolean tieneMayorPrioridadQue(NodoPrioridad<T> otro) {
            if (this.prioridad != otro.prioridad) {
                return this.prioridad < otro.prioridad;  // Menor número = mayor prioridad
            }
            // Si misma prioridad, el que llegó primero tiene prioridad
            return this.timestamp < otro.timestamp;
        }
    }
    
    public ColaPrioridad() {
        this.heap = new ArrayList<>();
        this.heap.add(null);  // Índice 0 no se usa
        this.tamanio = 0;
    }
    
    /**
     * Encolar un elemento con su prioridad
     * @param dato Elemento a encolar
     * @param prioridad 1 = Fast-Pass (alta), 2 = General (baja)
     */
    public void encolar(T dato, int prioridad) {
        NodoPrioridad<T> nuevo = new NodoPrioridad<>(dato, prioridad);
        heap.add(nuevo);
        tamanio++;
        
        // Flotar el elemento hacia arriba (heapify up)
        flotarArriba(tamanio);
    }
    
    /**
     * Desencolar el elemento de mayor prioridad
     */
    public T desencolar() {
        if (estaVacia()) {
            throw new IllegalStateException("La cola está vacía");
        }
        
        NodoPrioridad<T> raiz = heap.get(1);
        NodoPrioridad<T> ultimo = heap.get(tamanio);
        
        // Mover el último a la raíz
        heap.set(1, ultimo);
        heap.remove(tamanio);
        tamanio--;
        
        if (tamanio > 0) {
            // Hundir la nueva raíz (heapify down)
            hundirAbajo(1);
        }
        
        return raiz.getDato();
    }
    
    /**
     * Ver el elemento de mayor prioridad sin desencolar
     */
    public T verPrimero() {
        if (estaVacia()) {
            throw new IllegalStateException("La cola está vacía");
        }
        return heap.get(1).getDato();
    }
    
    /**
     * Obtener la prioridad del primer elemento
     */
    public int verPrioridadPrimero() {
        if (estaVacia()) {
            throw new IllegalStateException("La cola está vacía");
        }
        return heap.get(1).getPrioridad();
    }
    
    /**
     * Verificar si la cola está vacía
     */
    public boolean estaVacia() {
        return tamanio == 0;
    }
    
    /**
     * Obtener el tamaño de la cola
     */
    public int getTamanio() {
        return tamanio;
    }
    
    /**
     * Vaciar la cola
     */
    public void vaciar() {
        heap.clear();
        heap.add(null);
        tamanio = 0;
    }
    
    /**
     * Convertir la cola a una lista (sin modificar el orden)
     */
    public List<T> toList() {
        List<T> lista = new ArrayList<>();
        for (int i = 1; i <= tamanio; i++) {
            lista.add(heap.get(i).getDato());
        }
        return lista;
    }
    
    // ============= MÉTODOS PRIVADOS PARA EL HEAP =============
    
    private void flotarArriba(int indice) {
        while (indice > 1) {
            int padre = indice / 2;
            NodoPrioridad<T> nodoActual = heap.get(indice);
            NodoPrioridad<T> nodoPadre = heap.get(padre);
            
            if (nodoActual.tieneMayorPrioridadQue(nodoPadre)) {
                swap(indice, padre);
                indice = padre;
            } else {
                break;
            }
        }
    }
    
    private void hundirAbajo(int indice) {
        while (indice * 2 <= tamanio) {
            int hijoIzq = indice * 2;
            int hijoDer = indice * 2 + 1;
            int mayorPrioridad = hijoIzq;
            
            // Determinar cuál hijo tiene mayor prioridad
            if (hijoDer <= tamanio && 
                heap.get(hijoDer).tieneMayorPrioridadQue(heap.get(hijoIzq))) {
                mayorPrioridad = hijoDer;
            }
            
            // Si el padre tiene mayor prioridad que el hijo, terminar
            if (heap.get(indice).tieneMayorPrioridadQue(heap.get(mayorPrioridad))) {
                break;
            }
            
            swap(indice, mayorPrioridad);
            indice = mayorPrioridad;
        }
    }
    
    private void swap(int i, int j) {
        NodoPrioridad<T> temp = heap.get(i);
        heap.set(i, heap.get(j));
        heap.set(j, temp);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ColaPrioridad[");
        for (int i = 1; i <= tamanio; i++) {
            NodoPrioridad<T> nodo = heap.get(i);
            sb.append(String.format("%s(p:%d)", nodo.getDato(), nodo.getPrioridad()));
            if (i < tamanio) sb.append(", ");
        }
        sb.append("]");
        return sb.toString();
    }
}