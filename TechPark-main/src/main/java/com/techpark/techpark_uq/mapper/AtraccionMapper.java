package com.techpark.techpark_uq.mapper;

import com.techpark.techpark_uq.model.dto.AtraccionDTO;
import com.techpark.techpark_uq.model.entity.Atraccion;
import com.techpark.techpark_uq.model.entity.Zona;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface AtraccionMapper {

    @Mapping(source = "zona.id", target = "zonaId")
    @Mapping(source = "zona.nombre", target = "zonaNombre")
    @Mapping(source = "tipo", target = "tipo")
    @Mapping(source = "estado", target = "estado")
    AtraccionDTO toDto(Atraccion atraccion);

    @Mapping(source = "zonaId", target = "zona", qualifiedByName = "zonaIdToZona")
    @Mapping(target = "contadorVisitantes", ignore = true)
    Atraccion toEntity(AtraccionDTO atraccionDTO);

    @Named("zonaIdToZona")
    default Zona zonaIdToZona(Long zonaId) {
        if (zonaId == null)
            return null;
        Zona zona = new Zona();
        zona.setId(zonaId);
        return zona;
    }
}