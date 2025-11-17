package com.mb.conitrack.dto.mapper;

import com.mb.conitrack.dto.AnalisisDTO;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.Analisis;
import com.mb.conitrack.enums.TipoProductoEnum;
import org.thymeleaf.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Mapper para conversiones entre entidad Analisis y AnalisisDTO.
 */
public class AnalisisMapper {

    /**
     * Crea una entidad Analisis a partir de un MovimientoDTO.
     */
    public static Analisis createAnalisis(final MovimientoDTO dto) {
        final String nroAnalisis = StringUtils.isEmptyOrWhitespace(dto.getNroReanalisis())
                ? dto.getNroAnalisis()
                : dto.getNroReanalisis();
        if (nroAnalisis != null) {
            Analisis analisis = new Analisis();
            analisis.setFechaYHoraCreacion(dto.getFechaYHoraCreacion());
            analisis.setNroAnalisis(nroAnalisis);
            analisis.setObservaciones(dto.getObservaciones());
            analisis.setActivo(true);
            return analisis;
        }
        throw new IllegalArgumentException("El número de análisis es requerido");
    }

    /**
     * Convierte una lista de entidades Analisis a DTOs.
     */
    public static List<AnalisisDTO> fromEntities(final List<Analisis> analisisList) {
        final List<AnalisisDTO> analisisDtos = new ArrayList<>();
        for (Analisis entity : analisisList) {
            analisisDtos.add(fromEntity(entity));
        }
        return analisisDtos;
    }

    /**
     * Convierte una entidad Analisis a DTO.
     */
    public static AnalisisDTO fromEntity(Analisis entity) {
        if (entity == null) {
            return null;
        }
        AnalisisDTO dto = new AnalisisDTO();
        dto.setFechaYHoraCreacion(entity.getFechaYHoraCreacion());
        dto.setNroAnalisis(entity.getNroAnalisis());
        dto.setCodigoLote(entity.getLote().getCodigoLote());
        dto.setEsUnidadVenta(
                entity.getLote() != null &&
                        entity.getLote().getProducto() != null &&
                        entity.getLote().getProducto().getTipoProducto() == TipoProductoEnum.UNIDAD_VENTA);
        dto.setFechaRealizado(entity.getFechaRealizado());
        dto.setFechaReanalisis(entity.getFechaReanalisis());
        dto.setFechaVencimiento(entity.getFechaVencimiento());
        dto.setDictamen(entity.getDictamen());
        dto.setTitulo(entity.getTitulo());
        dto.setObservaciones(entity.getObservaciones());
        return dto;
    }
}
