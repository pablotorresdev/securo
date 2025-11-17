package com.mb.conitrack.dto.mapper;

import com.mb.conitrack.dto.DetalleMovimientoDTO;
import com.mb.conitrack.entity.DetalleMovimiento;

/**
 * Mapper para conversiones entre entidad DetalleMovimiento y DetalleMovimientoDTO.
 */
public class DetalleMovimientoMapper {

    /**
     * Convierte una entidad DetalleMovimiento a DTO.
     */
    public static DetalleMovimientoDTO fromEntity(DetalleMovimiento entity) {
        if (entity == null) {
            return null;
        }

        DetalleMovimientoDTO dto = new DetalleMovimientoDTO();
        dto.setCodigoMovimiento(entity.getMovimiento().getCodigoMovimiento());
        dto.setNroBulto(entity.getBulto().getNroBulto());
        dto.setCantidad(entity.getCantidad());
        dto.setUnidadMedida(entity.getUnidadMedida());

        return dto;
    }
}
