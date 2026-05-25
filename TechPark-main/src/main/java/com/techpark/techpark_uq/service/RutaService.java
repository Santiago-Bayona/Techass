package com.techpark.techpark_uq.service;

import com.techpark.techpark_uq.estructuras.Grafo;
import com.techpark.techpark_uq.estructuras.ListaEnlazada;
import com.techpark.techpark_uq.exception.BusinessException;
import com.techpark.techpark_uq.model.dto.PasoRutaDTO;
import com.techpark.techpark_uq.model.dto.RutaDTO;
import com.techpark.techpark_uq.model.dto.RutaMultipleDTO;
import com.techpark.techpark_uq.model.dto.SolicitudRutaDTO;
import com.techpark.techpark_uq.model.entity.Atraccion;
import com.techpark.techpark_uq.repository.AtraccionRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class RutaService {

    private final AtraccionRepository atraccionRepository;

    private final Grafo<Long> grafoParque = new Grafo<>();
    private final Map<Long, Atraccion> cacheAtracciones = new ConcurrentHashMap<>();

    private static final double VELOCIDAD_CAMINATA = 80.0;
    private static final double VELOCIDAD_CONGESTION = 50.0;
    private static final int UMBRAL_CONGESTION = 100;

    @PostConstruct
    public void inicializarMapa() {
        log.info("🗺️ Inicializando mapa del parque...");

        List<Atraccion> atracciones = atraccionRepository.findAll();

        if (atracciones.isEmpty()) {
            log.warn("No hay atracciones en la base de datos.");
            return;
        }

        // Agregar vértices
        for (Atraccion atraccion : atracciones) {
            cacheAtracciones.put(atraccion.getId(), atraccion);
            double x = atraccion.getPosicionX() != null ? atraccion.getPosicionX() : 0;
            double y = atraccion.getPosicionY() != null ? atraccion.getPosicionY() : 0;
            grafoParque.agregarVertice(atraccion.getId(), atraccion.getNombre(), x, y);
        }

        // Conectar atracciones
        conectarTodasLasAtracciones();

        log.info("✅ Mapa inicializado. Vértices: {}, Aristas: {}",
                grafoParque.getCantidadVertices(), grafoParque.getCantidadAristas());
    }

    private void conectarTodasLasAtracciones() {
        // Conexiones explícitas entre atracciones
        agregarArista(1, 2, 60, "Camino Montaña Rusa - Torre");
        agregarArista(2, 3, 50, "Camino Torre - Sillas");
        agregarArista(3, 1, 70, "Camino Sillas - Montaña Rusa");
        agregarArista(1, 4, 40, "Camino Montaña Rusa - Casa Terror");
        agregarArista(2, 4, 55, "Camino Torre - Casa Terror");
        
        // Zona Acuática
        agregarArista(5, 6, 45, "Camino Río - Tobogán");
        agregarArista(6, 7, 50, "Camino Tobogán - Piscina");
        agregarArista(7, 8, 40, "Camino Piscina - Rápido");
        agregarArista(8, 5, 55, "Camino Rápido - Río");
        agregarArista(5, 14, 65, "Camino Río - Circo");
        
        // Zona Infantil
        agregarArista(9, 10, 35, "Camino Carritos - Carrusel");
        agregarArista(10, 11, 40, "Camino Carrusel - Avioncitos");
        agregarArista(11, 9, 45, "Camino Avioncitos - Carritos");
        agregarArista(9, 12, 50, "Camino Carritos - Tren");
        agregarArista(10, 12, 30, "Camino Carrusel - Tren");
        
        // Zona Shows
        agregarArista(13, 14, 40, "Camino Magia - Circo");
        agregarArista(14, 15, 45, "Camino Circo - Cine");
        agregarArista(15, 16, 50, "Camino Cine - Concierto");
        agregarArista(13, 16, 55, "Camino Magia - Concierto");
        
        // Conexiones entre zonas
        agregarArista(4, 13, 60, "Camino Casa Terror - Show Magia");
        agregarArista(4, 14, 70, "Camino Casa Terror - Circo");
        agregarArista(8, 16, 50, "Camino Rápido - Concierto");
        agregarArista(12, 7, 55, "Camino Tren - Piscina");
        agregarArista(11, 3, 65, "Camino Avioncitos - Sillas");
        
        // Zona Gastronómica
        agregarArista(16, 17, 60, "Camino Concierto - Restaurante");
        agregarArista(17, 18, 40, "Camino Restaurante - Cafetería");
        agregarArista(18, 19, 35, "Camino Cafetería - Heladería");
        agregarArista(17, 14, 70, "Camino Restaurante - Circo");
        agregarArista(18, 15, 50, "Camino Cafetería - Cine");
        agregarArista(19, 11, 65, "Camino Heladería - Avioncitos");
        agregarArista(17, 19, 45, "Camino Restaurante - Heladería");
    }

    private void agregarArista(long origen, long destino, double distancia, String nombre) {
        if (cacheAtracciones.containsKey(origen) && cacheAtracciones.containsKey(destino)) {
            grafoParque.agregarArista(origen, destino, distancia, nombre);
            log.debug("Arista agregada: {} - {} ({}m)", origen, destino, distancia);
        }
    }

    public Map<Long, Map<String, Double>> obtenerPosicionesReales() {
        Map<Long, Map<String, Double>> posiciones = new HashMap<>();
        for (Atraccion a : cacheAtracciones.values()) {
            Map<String, Double> pos = new HashMap<>();
            pos.put("x", a.getPosicionX() != null ? a.getPosicionX() : 0.0);
            pos.put("y", a.getPosicionY() != null ? a.getPosicionY() : 0.0);
            posiciones.put(a.getId(), pos);
        }
        return posiciones;
    }

    public RutaDTO encontrarRutaMasCorta(SolicitudRutaDTO solicitud) {
        log.info("🔍 Buscando ruta de {} a {}", solicitud.getOrigenId(), solicitud.getDestinoId());

        Atraccion origen = cacheAtracciones.get(solicitud.getOrigenId());
        Atraccion destino = cacheAtracciones.get(solicitud.getDestinoId());

        if (origen == null) {
            throw new BusinessException("Atracción origen no encontrada", "ORIGEN_NO_ENCONTRADO");
        }
        if (destino == null) {
            throw new BusinessException("Atracción destino no encontrada", "DESTINO_NO_ENCONTRADO");
        }

        var resultado = grafoParque.dijkstra(solicitud.getOrigenId(), solicitud.getDestinoId());

        if (resultado.getCamino() == null || resultado.getCamino().getTamanio() == 0) {
            throw new BusinessException("No se encontró una ruta entre las atracciones", "RUTA_NO_ENCONTRADA");
        }

        return construirRespuestaRuta(origen, destino, resultado);
    }

    public RutaMultipleDTO encontrarRutaMultiple(List<Long> atraccionesIds) {
        if (atraccionesIds == null || atraccionesIds.size() < 2) {
            throw new BusinessException("Se necesitan al menos 2 atracciones", "NUMERO_ATRACCIONES_INSUFICIENTE");
        }

        List<RutaDTO> rutas = new ArrayList<>();
        double distanciaTotal = 0;
        int tiempoTotal = 0;

        for (int i = 0; i < atraccionesIds.size() - 1; i++) {
            SolicitudRutaDTO solicitud = new SolicitudRutaDTO();
            solicitud.setOrigenId(atraccionesIds.get(i));
            solicitud.setDestinoId(atraccionesIds.get(i + 1));

            try {
                RutaDTO ruta = encontrarRutaMasCorta(solicitud);
                rutas.add(ruta);
                distanciaTotal += ruta.getDistanciaTotal();
                tiempoTotal += ruta.getTiempoEstimadoTotal();
            } catch (BusinessException e) {
                throw new BusinessException("No es posible conectar todas las atracciones", "RUTA_INCOMPLETA");
            }
        }

        String sugerencia = generarSugerenciaRuta(distanciaTotal, tiempoTotal);

        return RutaMultipleDTO.builder()
                .atraccionesIds(atraccionesIds)
                .rutas(rutas)
                .distanciaTotal(distanciaTotal)
                .tiempoTotal(tiempoTotal)
                .sugerencia(sugerencia)
                .build();
    }

    public List<Map<String, Object>> encontrarAtraccionesCercanas(Long atraccionReferencia, int limite) {
        Atraccion referencia = cacheAtracciones.get(atraccionReferencia);
        if (referencia == null) {
            throw new BusinessException("Atracción de referencia no encontrada", "REFERENCIA_NO_ENCONTRADA");
        }

        var masCercanos = grafoParque.encontrarMasCercanos(atraccionReferencia, limite);
        List<Map<String, Object>> resultado = new ArrayList<>();

        for (var entry : masCercanos) {
            Atraccion atraccion = cacheAtracciones.get(entry.getValue());
            if (atraccion != null) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", atraccion.getId());
                item.put("nombre", atraccion.getNombre());
                item.put("tipo", atraccion.getTipo().toString());
                item.put("distancia", entry.getKey());
                item.put("tiempoEstimado", Math.round(entry.getKey() / VELOCIDAD_CAMINATA));
                resultado.add(item);
            }
        }
        return resultado;
    }

    public Map<String, Object> obtenerEstadoMapa() {
        Map<String, Object> estado = new HashMap<>();
        estado.put("vertices", grafoParque.getCantidadVertices());
        estado.put("aristas", grafoParque.getCantidadAristas());
        estado.put("esConexo", grafoParque.esConexo());
        return estado;
    }

    public Map<String, Object> obtenerMapaVisual() {
        Map<String, Object> mapa = new HashMap<>();
        List<Map<String, Object>> nodos = new ArrayList<>();
        List<Map<String, Object>> aristas = new ArrayList<>();
        Set<String> aristasAgregadas = new HashSet<>();

        for (Long id : grafoParque.obtenerVertices()) {
            Atraccion atraccion = cacheAtracciones.get(id);
            if (atraccion != null) {
                Map<String, Object> nodo = new HashMap<>();
                nodo.put("id", id);
                nodo.put("nombre", atraccion.getNombre());
                nodo.put("tipo", atraccion.getTipo().toString());
                nodo.put("x", atraccion.getPosicionX() != null ? atraccion.getPosicionX() : 0);
                nodo.put("y", atraccion.getPosicionY() != null ? atraccion.getPosicionY() : 0);
                nodos.add(nodo);
            }
        }

        for (Long id : grafoParque.obtenerVertices()) {
            var vecinos = grafoParque.obtenerVecinos(id);
            for (Grafo.Arista<Long> arista : vecinos) {
                String key = Math.min(id, arista.getDestino()) + "-" + Math.max(id, arista.getDestino());
                if (!aristasAgregadas.contains(key)) {
                    Map<String, Object> aristaMap = new HashMap<>();
                    aristaMap.put("origen", id);
                    aristaMap.put("destino", arista.getDestino());
                    aristaMap.put("distancia", arista.getPeso());
                    aristas.add(aristaMap);
                    aristasAgregadas.add(key);
                }
            }
        }

        mapa.put("nodos", nodos);
        mapa.put("aristas", aristas);
        return mapa;
    }

    public void refrescarMapa() {
        log.info("🔄 Refrescando mapa...");
        cacheAtracciones.clear();
        List<Atraccion> atracciones = atraccionRepository.findAll();
        for (Atraccion atraccion : atracciones) {
            cacheAtracciones.put(atraccion.getId(), atraccion);
        }
        inicializarMapa();
    }

    private RutaDTO construirRespuestaRuta(Atraccion origen, Atraccion destino,
                                           Grafo.ResultadoDijkstra<Long> resultado) {
        List<PasoRutaDTO> pasos = new ArrayList<>();
        ListaEnlazada<Long> camino = resultado.getCamino();
        List<Long> listaCamino = new ArrayList<>();
        
        for (Long id : camino) {
            listaCamino.add(id);
        }

        for (int i = 0; i < listaCamino.size(); i++) {
            Long idActual = listaCamino.get(i);
            Atraccion actual = cacheAtracciones.get(idActual);
            if (actual == null) continue;

            double distanciaDesdeAnterior = 0;
            Integer tiempoEstimado = 0;
            String instruccion = "";

            if (i > 0) {
                Long idAnterior = listaCamino.get(i - 1);
                distanciaDesdeAnterior = grafoParque.obtenerPeso(idAnterior, idActual);
                int visitantes = actual.getContadorVisitantes() != null ? actual.getContadorVisitantes() : 0;
                double velocidad = visitantes > UMBRAL_CONGESTION ? VELOCIDAD_CONGESTION : VELOCIDAD_CAMINATA;
                tiempoEstimado = (int) Math.round(distanciaDesdeAnterior / velocidad);
                instruccion = i == listaCamino.size() - 1 ? "🏁 ¡Llegaste a tu destino!" : "Continúa caminando";
            }

            pasos.add(PasoRutaDTO.builder()
                    .atraccionId(actual.getId())
                    .atraccionNombre(actual.getNombre())
                    .distanciaDesdeAnterior(distanciaDesdeAnterior)
                    .tiempoEstimado(tiempoEstimado)
                    .instruccion(instruccion)
                    .orden(i + 1)
                    .build());
        }

        double distanciaTotal = resultado.getDistanciaTotal() != null ? resultado.getDistanciaTotal() : 0;
        int tiempoTotal = (int) Math.round(distanciaTotal / VELOCIDAD_CAMINATA);
        String mensaje = generarMensajeRuta(distanciaTotal, tiempoTotal, origen.getNombre(), destino.getNombre());

        return RutaDTO.builder()
                .origenId(origen.getId())
                .origenNombre(origen.getNombre())
                .destinoId(destino.getId())
                .destinoNombre(destino.getNombre())
                .pasos(pasos)
                .distanciaTotal(distanciaTotal)
                .tiempoEstimadoTotal(tiempoTotal)
                .mensaje(mensaje)
                .build();
    }

    private String generarMensajeRuta(double distancia, int tiempo, String origen, String destino) {
        if (distancia <= 0) {
            return String.format("📍 %s y %s están en la misma ubicación", origen, destino);
        } else if (tiempo < 5) {
            return String.format("🎉 ¡Excelente! %s está muy cerca de %s.", destino, origen);
        } else if (tiempo < 15) {
            return String.format("🚶‍♂️ %s está a %d minutos de %s.", destino, tiempo, origen);
        } else {
            return String.format("🗺️ Para llegar de %s a %s caminarás aproximadamente %d minutos (%.0f metros).",
                    origen, destino, tiempo, distancia);
        }
    }

    private String generarSugerenciaRuta(double distanciaTotal, int tiempoTotal) {
        if (tiempoTotal < 30) return "✅ Ruta corta y cómoda.";
        if (tiempoTotal < 60) return "👍 Ruta de duración media. Lleva agua.";
        return "⚠️ Ruta larga. Considera descansar.";
    }
}