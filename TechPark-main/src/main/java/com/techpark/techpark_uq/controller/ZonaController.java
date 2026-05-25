package com.techpark.techpark_uq.controller;

import com.techpark.techpark_uq.model.dto.ApiResponseDTO;
import com.techpark.techpark_uq.model.entity.Zona;
import com.techpark.techpark_uq.repository.ZonaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/zonas")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ZonaController {

    private final ZonaRepository zonaRepository;

    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<Zona>>> listarTodas(
            HttpServletRequest request) {
        List<Zona> zonas = zonaRepository.findAll();
        return ResponseEntity.ok(ApiResponseDTO.success(
            zonas,
            "Zonas listadas exitosamente",
            request.getRequestURI()
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Zona>> obtenerPorId(
            @PathVariable Long id,
            HttpServletRequest request) {
        Zona zona = zonaRepository.findById(id).orElse(null);
        return ResponseEntity.ok(ApiResponseDTO.success(
            zona,
            "Zona encontrada",
            request.getRequestURI()
        ));
    }
}