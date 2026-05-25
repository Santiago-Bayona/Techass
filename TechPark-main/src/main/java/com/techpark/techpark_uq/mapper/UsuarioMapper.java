package com.techpark.techpark_uq.mapper;

import com.techpark.techpark_uq.model.dto.UsuarioDTO;
import com.techpark.techpark_uq.model.dto.RegistroVisitanteRequest;
import com.techpark.techpark_uq.model.entity.Usuario;
import com.techpark.techpark_uq.model.entity.Visitante;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    UsuarioMapper INSTANCE = Mappers.getMapper(UsuarioMapper.class);

    // ← AGREGAR ESTE MÉTODO ESPECÍFICO PARA VISITANTE
    default UsuarioDTO toDto(Usuario usuario) {
        if (usuario == null) {
            return null;
        }

        UsuarioDTO dto = new UsuarioDTO();
        dto.setId(usuario.getId());
        dto.setNombre(usuario.getNombre());
        dto.setDocumento(usuario.getDocumento());
        dto.setEmail(usuario.getEmail());
        dto.setPassword(usuario.getPassword());
        dto.setEdad(usuario.getEdad());
        dto.setEstatura(usuario.getEstatura());
        dto.setRol(usuario.getRol() != null ? usuario.getRol().toString() : null);
        dto.setFotoUrl(usuario.getFotoUrl());
        dto.setSaldoVirtual(usuario.getSaldoVirtual());
        dto.setActivo(usuario.getActivo());

        // ← MAPEAR CAMPOS ESPECÍFICOS DE VISITANTE
        if (usuario instanceof Visitante) {
            Visitante visitante = (Visitante) usuario;
            dto.setTicketActivo(visitante.getTicketActivo());
        }

        return dto;
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaRegistro", ignore = true)
    @Mapping(target = "activo", constant = "true")
    @Mapping(target = "rol", constant = "VISITANTE")
    Visitante toEntity(RegistroVisitanteRequest request);
}
