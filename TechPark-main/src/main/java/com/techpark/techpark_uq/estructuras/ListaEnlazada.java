package com.techpark.techpark_uq.estructuras;

import lombok.Getter;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Implementación propia de Lista Enlazada Simple
 * @param <T> Tipo de dato de los elementos
 */
public class ListaEnlazada<T> implements Iterable<T> {
    
    @Getter
    private Nodo<T> primero;
    @Getter
    private Nodo<T> ultimo;
    private int tamanio;
    
    public ListaEnlazada() {
        this.primero = null;
        this.ultimo = null;
        this.tamanio = 0;
    }
    
    // ============= CLASE INTERNA NODO =============
    @Getter
    public static class Nodo<T> {
        private final T dato;
        private Nodo<T> siguiente;
        
        public Nodo(T dato) {
            this.dato = dato;
            this.siguiente = null;
        }
        
        @Override
        public String toString() {
            return dato.toString();
        }
    }
    
    // ============= OPERACIONES BÁSICAS =============
    
    /**
     * Agregar un elemento al final de la lista
     */
    public void agregar(T dato) {
        Nodo<T> nuevo = new Nodo<>(dato);
        
        if (primero == null) {
            primero = nuevo;
            ultimo = nuevo;
        } else {
            ultimo.siguiente = nuevo;
            ultimo = nuevo;
        }
        tamanio++;
    }
    
    /**
     * Agregar un elemento al inicio de la lista
     */
    public void agregarAlInicio(T dato) {
        Nodo<T> nuevo = new Nodo<>(dato);
        
        if (primero == null) {
            primero = nuevo;
            ultimo = nuevo;
        } else {
            nuevo.siguiente = primero;
            primero = nuevo;
        }
        tamanio++;
    }
    
    /**
     * Agregar un elemento en una posición específica
     */
    public void agregarEnPosicion(T dato, int posicion) {
        if (posicion < 0 || posicion > tamanio) {
            throw new IndexOutOfBoundsException("Posición inválida: " + posicion);
        }
        
        if (posicion == 0) {
            agregarAlInicio(dato);
            return;
        }
        
        if (posicion == tamanio) {
            agregar(dato);
            return;
        }
        
        Nodo<T> nuevo = new Nodo<>(dato);
        Nodo<T> actual = primero;
        
        for (int i = 0; i < posicion - 1; i++) {
            actual = actual.siguiente;
        }
        
        nuevo.siguiente = actual.siguiente;
        actual.siguiente = nuevo;
        tamanio++;
    }
    
    /**
     * Eliminar un elemento por su valor (primera ocurrencia)
     */
    public boolean eliminar(T dato) {
        if (primero == null) return false;
        
        // Si el elemento está al inicio
        if (primero.dato.equals(dato)) {
            primero = primero.siguiente;
            if (primero == null) {
                ultimo = null;
            }
            tamanio--;
            return true;
        }
        
        // Buscar en el resto
        Nodo<T> actual = primero;
        while (actual.siguiente != null && !actual.siguiente.dato.equals(dato)) {
            actual = actual.siguiente;
        }
        
        if (actual.siguiente != null) {
            actual.siguiente = actual.siguiente.siguiente;
            if (actual.siguiente == null) {
                ultimo = actual;
            }
            tamanio--;
            return true;
        }
        
        return false;
    }
    
    /**
     * Eliminar un elemento por su posición
     */
    public T eliminarEnPosicion(int posicion) {
        if (posicion < 0 || posicion >= tamanio) {
            throw new IndexOutOfBoundsException("Posición inválida: " + posicion);
        }
        
        T datoEliminado;
        
        if (posicion == 0) {
            datoEliminado = primero.dato;
            primero = primero.siguiente;
            if (primero == null) {
                ultimo = null;
            }
            tamanio--;
            return datoEliminado;
        }
        
        Nodo<T> actual = primero;
        for (int i = 0; i < posicion - 1; i++) {
            actual = actual.siguiente;
        }
        
        datoEliminado = actual.siguiente.dato;
        actual.siguiente = actual.siguiente.siguiente;
        
        if (actual.siguiente == null) {
            ultimo = actual;
        }
        
        tamanio--;
        return datoEliminado;
    }
    
    /**
     * Obtener un elemento por su posición
     */
    public T obtener(int posicion) {
        if (posicion < 0 || posicion >= tamanio) {
            throw new IndexOutOfBoundsException("Posición inválida: " + posicion);
        }
        
        Nodo<T> actual = primero;
        for (int i = 0; i < posicion; i++) {
            actual = actual.siguiente;
        }
        return actual.dato;
    }
    
    /**
     * Buscar la posición de un elemento
     */
    public int buscar(T dato) {
        Nodo<T> actual = primero;
        int posicion = 0;
        
        while (actual != null) {
            if (actual.dato.equals(dato)) {
                return posicion;
            }
            actual = actual.siguiente;
            posicion++;
        }
        
        return -1;
    }
    
    /**
     * Verificar si la lista contiene un elemento
     */
    public boolean contiene(T dato) {
        return buscar(dato) != -1;
    }
    
    /**
     * Obtener el tamaño de la lista
     */
    public int getTamanio() {
        return tamanio;
    }
    
    /**
     * Verificar si la lista está vacía
     */
    public boolean estaVacia() {
        return tamanio == 0;
    }
    
    /**
     * Vaciar la lista
     */
    public void vaciar() {
        primero = null;
        ultimo = null;
        tamanio = 0;
    }
    
    /**
     * Convertir a array
     */
    @SuppressWarnings("unchecked")
    public T[] toArray() {
        T[] array = (T[]) new Object[tamanio];
        Nodo<T> actual = primero;
        int i = 0;
        while (actual != null) {
            array[i++] = actual.dato;
            actual = actual.siguiente;
        }
        return array;
    }
    
    /**
     * Revertir la lista
     */
    public void revertir() {
        Nodo<T> previo = null;
        Nodo<T> actual = primero;
        Nodo<T> siguiente;
        
        ultimo = primero;
        
        while (actual != null) {
            siguiente = actual.siguiente;
            actual.siguiente = previo;
            previo = actual;
            actual = siguiente;
        }
        
        primero = previo;
    }
    
    // ============= ITERADOR =============
    
    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private Nodo<T> actual = primero;
            
            @Override
            public boolean hasNext() {
                return actual != null;
            }
            
            @Override
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                T dato = actual.dato;
                actual = actual.siguiente;
                return dato;
            }
        };
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        Nodo<T> actual = primero;
        while (actual != null) {
            sb.append(actual.dato);
            if (actual.siguiente != null) {
                sb.append(" → ");
            }
            actual = actual.siguiente;
        }
        sb.append("]");
        return sb.toString();
    }
}