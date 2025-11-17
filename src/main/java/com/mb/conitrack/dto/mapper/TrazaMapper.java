package com.mb.conitrack.dto.mapper;

import com.mb.conitrack.dto.TrazaDTO;
import com.mb.conitrack.entity.Traza;

import java.util.ArrayList;
import java.util.List;

/**
 * Mapper para conversiones entre entidad Traza y TrazaDTO.
 */
public class TrazaMapper {

    /**
     * Convierte una lista de entidades Traza a DTOs.
     */
    public static List<TrazaDTO> fromEntities(final List<Traza> trazaList) {
        final List<TrazaDTO> trazaDTOs = new ArrayList<>();
        for (Traza entity : trazaList) {
            trazaDTOs.add(fromEntity(entity));
        }
        return trazaDTOs;
    }

    /**
     * Convierte una entidad Traza a DTO.
     */
    public static TrazaDTO fromEntity(Traza entity) {
        if (entity == null) {
            return null;
        }
        TrazaDTO dto = new TrazaDTO();
        dto.setFechaYHoraCreacion(entity.getFechaYHoraCreacion());
        dto.setCodigoProducto(entity.getProducto().getCodigoProducto());
        dto.setCodigoLote(entity.getLote().getCodigoLote());
        dto.setNroBulto(entity.getBulto().getNroBulto());
        dto.setEstado(entity.getEstado());
        dto.setNroTraza(entity.getNroTraza());
        dto.setObservaciones(entity.getObservaciones());
        return dto;
    }
}
