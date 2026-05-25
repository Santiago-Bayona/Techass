package com.techpark.techpark_uq.estructuras;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Implementación propia de Conjunto usando Hash Table
 * Para almacenar atracciones favoritas sin duplicados
 * @param <T> Tipo de dato de los elementos
 */
public class Conjunto<T> implements Iterable<T> {
    
    private static final int CAPACIDAD_INICIAL = 16;
    private static final double FACTOR_CARGA = 0.75;
    
    private ListaEnlazada<T>[] buckets;
    private int tamanio;
    
    @SuppressWarnings("unchecked")
    public Conjunto() {
        this.buckets = new ListaEnlazada[CAPACIDAD_INICIAL];
        this.tamanio = 0;
    }
    
    // ============= FUNCIÓN HASH =============
    
    private int obtenerIndice(T elemento) {
        int hashCode = elemento != null ? elemento.hashCode() : 0;
        return Math.abs(hashCode) % buckets.length;
    }
    
    // ============= OPERACIONES BÁSICAS =============
    
    public boolean agregar(T elemento) {
        if (contiene(elemento)) {
            return false;
        }
        
        if ((double) tamanio / buckets.length >= FACTOR_CARGA) {
            redimensionar();
        }
        
        int indice = obtenerIndice(elemento);
        if (buckets[indice] == null) {
            buckets[indice] = new ListaEnlazada<>();
        }
        
        buckets[indice].agregar(elemento);
        tamanio++;
        return true;
    }
    
    public boolean eliminar(T elemento) {
        int indice = obtenerIndice(elemento);
        ListaEnlazada<T> bucket = buckets[indice];
        
        if (bucket == null) {
            return false;
        }
        
        boolean eliminado = bucket.eliminar(elemento);
        if (eliminado) {
            tamanio--;
        }
        return eliminado;
    }
    
    public boolean contiene(T elemento) {
        int indice = obtenerIndice(elemento);
        ListaEnlazada<T> bucket = buckets[indice];
        
        if (bucket == null) {
            return false;
        }
        
        return bucket.contiene(elemento);
    }
    
    public int getTamanio() {
        return tamanio;
    }
    
    public boolean estaVacio() {
        return tamanio == 0;
    }
    
    public void vaciar() {
        for (int i = 0; i < buckets.length; i++) {
            if (buckets[i] != null) {
                buckets[i].vaciar();
            }
        }
        tamanio = 0;
    }
    
    // ============= OPERACIONES DE CONJUNTOS =============
    
    public Conjunto<T> union(Conjunto<T> otro) {
        Conjunto<T> resultado = new Conjunto<>();
        
        for (T elemento : this) {
            resultado.agregar(elemento);
        }
        
        for (T elemento : otro) {
            resultado.agregar(elemento);
        }
        
        return resultado;
    }
    
    public Conjunto<T> interseccion(Conjunto<T> otro) {
        Conjunto<T> resultado = new Conjunto<>();
        
        for (T elemento : this) {
            if (otro.contiene(elemento)) {
                resultado.agregar(elemento);
            }
        }
        
        return resultado;
    }
    
    public Conjunto<T> diferencia(Conjunto<T> otro) {
        Conjunto<T> resultado = new Conjunto<>();
        
        for (T elemento : this) {
            if (!otro.contiene(elemento)) {
                resultado.agregar(elemento);
            }
        }
        
        return resultado;
    }
    
    public boolean esSubconjunto(Conjunto<T> otro) {
        for (T elemento : this) {
            if (!otro.contiene(elemento)) {
                return false;
            }
        }
        return true;
    }
    
    // ============= MÉTODOS PRIVADOS =============
    
    @SuppressWarnings("unchecked")
    private void redimensionar() {
        int nuevaCapacidad = buckets.length * 2;
        ListaEnlazada<T>[] nuevosBuckets = new ListaEnlazada[nuevaCapacidad];
        
        for (ListaEnlazada<T> bucket : buckets) {
            if (bucket != null) {
                for (T elemento : bucket) {
                    int nuevoIndice = elemento != null ? Math.abs(elemento.hashCode()) % nuevaCapacidad : 0;
                    if (nuevosBuckets[nuevoIndice] == null) {
                        nuevosBuckets[nuevoIndice] = new ListaEnlazada<>();
                    }
                    nuevosBuckets[nuevoIndice].agregar(elemento);
                }
            }
        }
        
        buckets = nuevosBuckets;
    }
    
    public ListaEnlazada<T> toList() {
        ListaEnlazada<T> lista = new ListaEnlazada<>();
        for (T elemento : this) {
            lista.agregar(elemento);
        }
        return lista;
    }
    
    // ============= ITERADOR (CORREGIDO) =============
    
    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private int bucketActual = 0;
            private Iterator<T> iteradorActual = null;
            private int elementosRecorridos = 0;
            
            private void avanzarAlSiguienteBucket() {
                while (bucketActual < buckets.length && elementosRecorridos < tamanio) {
                    if (buckets[bucketActual] != null && buckets[bucketActual].getTamanio() > 0) {
                        iteradorActual = buckets[bucketActual].iterator();
                        if (iteradorActual != null && iteradorActual.hasNext()) {
                            return;
                        }
                    }
                    bucketActual++;
                }
                iteradorActual = null;
            }
            
            @Override
            public boolean hasNext() {
                if (elementosRecorridos >= tamanio) {
                    return false;
                }
                
                if (iteradorActual == null) {
                    bucketActual = 0;
                    avanzarAlSiguienteBucket();
                }
                
                if (iteradorActual != null && iteradorActual.hasNext()) {
                    return true;
                }
                
                // Buscar siguiente bucket
                bucketActual++;
                avanzarAlSiguienteBucket();
                
                return iteradorActual != null && iteradorActual.hasNext();
            }
            
            @Override
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                
                T elemento = iteradorActual.next();
                elementosRecorridos++;
                return elemento;
            }
        };
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Conjunto{");
        boolean primero = true;
        for (T elemento : this) {
            if (!primero) {
                sb.append(", ");
            }
            sb.append(elemento);
            primero = false;
        }
        sb.append(", tamaño=").append(tamanio).append("}");
        return sb.toString();
    }
}