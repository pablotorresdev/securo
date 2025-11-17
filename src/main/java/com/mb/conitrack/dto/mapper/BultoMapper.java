package com.mb.conitrack.dto.mapper;

import com.mb.conitrack.dto.BultoDTO;
import com.mb.conitrack.entity.Bulto;
import com.mb.conitrack.entity.DetalleMovimiento;

import java.util.ArrayList;
import java.util.List;

/**
 * Mapper para conversiones entre entidad Bulto y BultoDTO.
 */
public class BultoMapper {

    /**
     * Convierte una lista de entidades Bulto a DTOs.
     */
    public static List<BultoDTO> fromEntities(final List<Bulto> bultoList) {
        final List<BultoDTO> bultoDTOs = new ArrayList<>();
        for (Bulto entity : bultoList) {
            bultoDTOs.add(fromEntity(entity));
        }
        return bultoDTOs;
    }

    /**
     * Convierte una entidad Bulto a DTO.
     */
    public static BultoDTO fromEntity(Bulto entity) {
        if (entity == null) {
            return null;
        }

        BultoDTO dto = new BultoDTO();
        dto.setNroBulto(entity.getNroBulto());
        dto.setCantidadActual(entity.getCantidadActual());
        dto.setCantidadInicial(entity.getCantidadInicial());
        dto.setUnidadMedida(entity.getUnidadMedida());
        dto.setCodigoLote(entity.getLote().getCodigoLote());
        dto.setEstado(entity.getEstado());

        for (DetalleMovimiento detalleMovimiento : entity.getDetalles()) {
            dto.getDetalleMovimientoDTOs().add(DetalleMovimientoMapper.fromEntity(detalleMovimiento));
        }

        return dto;
    }
}
