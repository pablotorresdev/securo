package com.mb.conitrack.dto.mapper;

import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.DetalleMovimiento;
import com.mb.conitrack.entity.Movimiento;

import java.util.ArrayList;
import java.util.List;

/**
 * Mapper para conversiones entre entidad Movimiento y MovimientoDTO.
 */
public class MovimientoMapper {

    /**
     * Convierte una lista de entidades Movimiento a DTOs.
     */
    public static List<MovimientoDTO> fromEntities(final List<Movimiento> movimientoList) {
        final List<MovimientoDTO> movimientoDtos = new ArrayList<>();
        for (Movimiento entity : movimientoList) {
            movimientoDtos.add(fromEntity(entity));
        }
        return movimientoDtos;
    }

    /**
     * Convierte una entidad Movimiento a DTO.
     */
    public static MovimientoDTO fromEntity(Movimiento entity) {
        if (entity == null) {
            return null;
        }

        MovimientoDTO dto = new MovimientoDTO();
        dto.setFechaMovimiento(entity.getFecha());
        dto.setCodigoMovimiento(entity.getCodigoMovimiento());
        if (entity.getLote() != null) {
            dto.setCodigoLote(entity.getLote().getCodigoLote());
            dto.setLoteId(entity.getLote().getId());
            for (DetalleMovimiento detalleMovimiento : entity.getDetalles()) {
                dto.getDetalleMovimientoDTOs().add(DetalleMovimientoMapper.fromEntity(detalleMovimiento));
            }
        }
        if (entity.getMovimientoOrigen() != null) {
            dto.setCodigoMovimientoOrigen(entity.getMovimientoOrigen().getCodigoMovimiento());
        }
        dto.setCantidad(entity.getCantidad());
        dto.setUnidadMedida(entity.getUnidadMedida());

        dto.setObservaciones(entity.getObservaciones());
        dto.setNroAnalisis(entity.getNroAnalisis());
        dto.setOrdenProduccion(entity.getOrdenProduccion());

        if (entity.getTipoMovimiento() != null) {
            dto.setTipoMovimiento(entity.getTipoMovimiento());
        }
        if (entity.getMotivo() != null) {
            dto.setMotivo(entity.getMotivo());
        }

        dto.setDictamenInicial(entity.getDictamenInicial());
        dto.setDictamenFinal(entity.getDictamenFinal());

        return dto;
    }
}
