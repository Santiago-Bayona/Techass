package com.techpark.techpark_uq.controller;

import com.techpark.techpark_uq.model.dto.ApiResponseDTO;
import com.techpark.techpark_uq.model.dto.RespuestaColaDTO;
import com.techpark.techpark_uq.model.dto.SolicitudColaDTO;
import com.techpark.techpark_uq.model.entity.ColaVirtual;
import com.techpark.techpark_uq.model.entity.RolUsuario;
import com.techpark.techpark_uq.model.entity.Usuario;
import com.techpark.techpark_uq.repository.ColaVirtualRepository;
import com.techpark.techpark_uq.repository.UsuarioRepository;
import com.techpark.techpark_uq.service.ColaVirtualService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/colas")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ColaVirtualController {

        private final ColaVirtualService colaVirtualService;
        private final ColaVirtualRepository colaVirtualRepository;
        private final UsuarioRepository usuarioRepository; // ← Agregar esta línea

        /**
         * Obtener todas las colas activas de un visitante
         */
        @GetMapping("/mis-colas/{visitanteId}")
        @PreAuthorize("hasRole('VISITANTE')")
        public ResponseEntity<ApiResponseDTO<List<Map<String, Object>>>> obtenerMisColas(
                        @PathVariable Long visitanteId,
                        HttpServletRequest request) {

                log.info("📋 Obteniendo colas activas para visitante: {}", visitanteId);

                // Obtener el usuario autenticado
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                String email = auth.getName();
                log.info("Usuario autenticado: {}", email);

                // Buscar el visitante por email para verificar que el ID coincide
                Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
                if (usuarioOpt.isEmpty()) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                        .body(ApiResponseDTO.error("No autorizado", request.getRequestURI()));
                }

                Usuario usuario = usuarioOpt.get();
                if (!usuario.getId().equals(visitanteId) && usuario.getRol() != RolUsuario.ADMINISTRADOR) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                        .body(ApiResponseDTO.error("No tienes permisos para ver estas colas",
                                                        request.getRequestURI()));
                }

                List<ColaVirtual> colas = colaVirtualRepository.findColasActivasByVisitanteId(visitanteId);

                List<Map<String, Object>> respuesta = new ArrayList<>();
                for (ColaVirtual cola : colas) {
                        Map<String, Object> item = new HashMap<>();
                        item.put("id", cola.getId());
                        item.put("atraccionId", cola.getAtraccion().getId());
                        item.put("atraccionNombre", cola.getAtraccion().getNombre());
                        item.put("posicion", cola.getPosicion());
                        item.put("tiempoEstimado", cola.getAtraccion().getTiempoEsperaEstimado());
                        item.put("fechaIngreso", cola.getHoraIngresoCola());
                        item.put("prioridad", cola.getPrioridad());
                        item.put("estado", cola.getAtendido() ? "ATENDIDO" : "EN_COLA");
                        respuesta.add(item);
                }

                log.info("✅ Se encontraron {} colas activas para visitante {}", respuesta.size(), visitanteId);

                return ResponseEntity.ok(ApiResponseDTO.success(
                                respuesta,
                                "Colas obtenidas exitosamente",
                                request.getRequestURI()));
        }

        @PostMapping("/unirse")
        @PreAuthorize("hasRole('VISITANTE')")
        public ResponseEntity<ApiResponseDTO<RespuestaColaDTO>> unirseACola(
                        @Valid @RequestBody SolicitudColaDTO solicitud,
                        HttpServletRequest request) {

                RespuestaColaDTO respuesta = colaVirtualService.unirseACola(solicitud);

                return ResponseEntity.ok(ApiResponseDTO.success(
                                respuesta,
                                "Te has unido a la cola correctamente",
                                request.getRequestURI()));
        }

        @GetMapping("/siguiente/{atraccionId}/{operadorId}")
        @PreAuthorize("hasAnyRole('OPERADOR', 'ADMINISTRADOR')")
        public ResponseEntity<ApiResponseDTO<Map<String, Object>>> siguienteEnCola(
                        @PathVariable Long atraccionId,
                        @PathVariable Long operadorId,
                        HttpServletRequest request) {

                ColaVirtualService.ElementoCola siguiente = colaVirtualService.siguienteEnCola(atraccionId, operadorId);

                Map<String, Object> respuesta = Map.of(
                                "visitanteId", siguiente.getVisitanteId(),
                                "nombreVisitante", siguiente.getNombreVisitante(),
                                "tipoTicket", siguiente.getTipoTicket(),
                                "horaIngreso", siguiente.getHoraIngreso());

                return ResponseEntity.ok(ApiResponseDTO.success(
                                respuesta,
                                "Visitante atendido correctamente",
                                request.getRequestURI()));
        }

        @DeleteMapping("/cancelar/{visitanteId}/{atraccionId}")
        @PreAuthorize("hasRole('VISITANTE')")
        public ResponseEntity<ApiResponseDTO<Boolean>> cancelarCola(
                        @PathVariable Long visitanteId,
                        @PathVariable Long atraccionId,
                        HttpServletRequest request) {

                boolean cancelado = colaVirtualService.cancelarCola(visitanteId, atraccionId);

                if (cancelado) {
                        return ResponseEntity.ok(ApiResponseDTO.success(
                                        true,
                                        "Has cancelado tu posición en la cola",
                                        request.getRequestURI()));
                } else {
                        return ResponseEntity.ok(ApiResponseDTO.success(
                                        false,
                                        "No se encontró una posición activa en la cola",
                                        request.getRequestURI()));
                }
        }

        @GetMapping("/estado/{atraccionId}")
        @PreAuthorize("isAuthenticated()")
        public ResponseEntity<ApiResponseDTO<Map<String, Object>>> obtenerEstadoCola(
                        @PathVariable Long atraccionId,
                        HttpServletRequest request) {

                Map<String, Object> estado = colaVirtualService.obtenerEstadoCola(atraccionId);

                return ResponseEntity.ok(ApiResponseDTO.success(
                                estado,
                                "Estado de cola obtenido correctamente",
                                request.getRequestURI()));
        }

        @GetMapping("/posicion/{visitanteId}/{atraccionId}")
        @PreAuthorize("hasRole('VISITANTE')")
        public ResponseEntity<ApiResponseDTO<Integer>> obtenerPosicion(
                        @PathVariable Long visitanteId,
                        @PathVariable Long atraccionId,
                        HttpServletRequest request) {

                Integer posicion = colaVirtualService.obtenerPosicionVisitante(visitanteId, atraccionId);

                return ResponseEntity.ok(ApiResponseDTO.success(
                                posicion,
                                posicion != null ? "Posición obtenida" : "No estás en la cola",
                                request.getRequestURI()));
        }

        @GetMapping("/todas")
        @PreAuthorize("hasAnyRole('OPERADOR', 'ADMINISTRADOR')")
        public ResponseEntity<ApiResponseDTO<Map<Long, Map<String, Object>>>> obtenerTodasLasColas(
                        HttpServletRequest request) {

                Map<Long, Map<String, Object>> todasLasColas = colaVirtualService.obtenerTodasLasColas();

                return ResponseEntity.ok(ApiResponseDTO.success(
                                todasLasColas,
                                "Colas activas obtenidas",
                                request.getRequestURI()));
        }


        /*                                     */
        @GetMapping("/debug/atraccion/{atraccionId}")
        @PreAuthorize("hasAnyRole('OPERADOR', 'ADMINISTRADOR')")
        public ResponseEntity<ApiResponseDTO<List<Map<String, Object>>>> debugColasPorAtraccion(
                        @PathVariable Long atraccionId,
                        HttpServletRequest request) {

                List<ColaVirtual> colas = colaVirtualRepository
                                .findByAtraccionIdAndAtendidoFalseOrderByPrioridadAscHoraIngresoColaAsc(atraccionId);

                List<Map<String, Object>> respuesta = new ArrayList<>();
                for (ColaVirtual cola : colas) {
                        Map<String, Object> item = new HashMap<>();
                        item.put("id", cola.getId());
                        item.put("visitanteId", cola.getVisitante().getId());
                        item.put("visitanteNombre", cola.getVisitante().getNombre());
                        item.put("prioridad", cola.getPrioridad());
                        item.put("horaIngreso", cola.getHoraIngresoCola());
                        respuesta.add(item);
                }

                return ResponseEntity.ok(ApiResponseDTO.success(
                                respuesta,
                                "Colas encontradas: " + respuesta.size(),
                                request.getRequestURI()));
        }
}