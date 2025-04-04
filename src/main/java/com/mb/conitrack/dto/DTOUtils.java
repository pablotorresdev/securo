package com.mb.conitrack.dto;

import java.util.List;

import org.thymeleaf.util.StringUtils;

import com.mb.conitrack.entity.Analisis;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.entity.maestro.Producto;

public class DTOUtils {

    public static AnalisisDTO fromEntity(Analisis entity) {
        if (entity == null) {
            return null;
        }
        AnalisisDTO dto = new AnalisisDTO();
        dto.setFechaAnalisis(entity.getFechaAnalisis());
        dto.setFechaYHoraCreacion(entity.getFechaYHoraCreacion());
        dto.setNroAnalisis(entity.getNroAnalisis());
        dto.setDictamen(entity.getDictamen());
        return dto;
    }

    public static MovimientoDTO fromEntity(Movimiento entity) {
        if (entity == null) {
            return null;
        }

        MovimientoDTO dto = new MovimientoDTO();
        dto.setFechaMovimiento(entity.getFecha());
        if (entity.getLote() != null) {
            dto.setLoteId(entity.getLote().getId());
        } else {
            dto.setLoteId(-1l);
        }

        dto.setCantidad(entity.getCantidad());
        dto.setUnidadMedida(entity.getUnidadMedida());

        dto.setObservaciones(entity.getObservaciones());
        dto.setNroAnalisis(entity.getNroAnalisis());
        dto.setNroReAnalisis(entity.getNroAnalisis());
        dto.setOrdenProduccion(entity.getOrdenProduccion());

        if (entity.getTipoMovimiento() != null) {
            dto.setTipoMovimiento(entity.getTipoMovimiento().name());
        }
        if (entity.getMotivo() != null) {
            dto.setMotivo(entity.getMotivo().name());
        }

        dto.setDictamenInicial(entity.getDictamenInicial());
        dto.setDictamenFinal(entity.getDictamenFinal());
        return dto;
    }


    public static Analisis createAnalisis(final MovimientoDTO dto) {
        final String nroAnalisis = StringUtils.isEmpty(dto.getNroReAnalisis()) ? dto.getNroAnalisis() : dto.getNroReAnalisis();
        if (nroAnalisis != null) {
            Analisis analisis = new Analisis();
            analisis.setFechaYHoraCreacion(dto.getFechaYHoraCreacion());
            analisis.setFechaAnalisis(dto.getFechaAnalisis());
            analisis.setNroAnalisis(nroAnalisis);
            analisis.setObservaciones(dto.getObservaciones());
            analisis.setActivo(true);
            return analisis;
        }
        throw new IllegalArgumentException("El número de análisis es requerido");
    }


    public static LoteDTO fromEntities(List<Lote> entities) {
        if (entities == null || entities.isEmpty()) {
            return null;
        }

        LoteDTO dto = new LoteDTO();
        boolean firstCase = true;
        for (Lote entity : entities) {
            if (firstCase) {
                if (entity.getProducto() != null) {
                    final Producto producto = entity.getProducto();
                    dto.setProductoId(producto.getId());
                    dto.setNombreProducto(producto.getNombreGenerico());
                    dto.setCodigoProducto(producto.getCodigoInterno());
                    dto.setTipoProducto(producto.getTipoProducto());
                    dto.setProductoDestino(producto.getProductoDestino() != null ? producto.getProductoDestino().getNombreGenerico() : null);
                }
                dto.setProveedorId(entity.getProveedor() != null ? entity.getProveedor().getId() : null);
                dto.setNombreProveedor(entity.getProveedor() != null ? entity.getProveedor().getRazonSocial() : null);
                dto.setFechaIngreso(entity.getFechaIngreso());
                dto.setFechaYHoraCreacion(entity.getFechaYHoraCreacion());
                dto.setBultosTotales(entity.getBultosTotales());

                //Del 1ero solo para que no falle por null validation
                dto.setCantidadInicial(entity.getCantidadInicial());
                dto.setUnidadMedida(entity.getUnidadMedida());

                dto.setLoteProveedor(entity.getLoteProveedor());

                dto.setNroRemito(entity.getNroRemito());
                dto.setDetalleConservacion(entity.getDetalleConservacion());
                dto.setFechaVencimiento(entity.getFechaVencimiento());
                dto.setFechaReanalisis(entity.getFechaReanalisis());
                dto.setTitulo(entity.getTitulo());
                dto.setObservaciones(entity.getObservaciones());

                dto.getCantidadesBultos().add(entity.getCantidadInicial());
                dto.getUnidadMedidaBultos().add(entity.getUnidadMedida());

                addMovimientosDTO(dto, entity);
                addAnalisisDTO(dto, entity);

                firstCase = false;
            } else {
                dto.getCantidadesBultos().add(entity.getNroBulto() - 1, entity.getCantidadInicial());
                dto.getUnidadMedidaBultos().add(entity.getNroBulto() - 1, entity.getUnidadMedida());

                addMovimientosDTO(dto, entity);
                addAnalisisDTO(dto, entity);
            }
        }
        return dto;
    }

    private static void addMovimientosDTO(final LoteDTO dto, final Lote entity) {
        for (Movimiento movimiento : entity.getMovimientos()) {
            final MovimientoDTO movimientoDTO = DTOUtils.fromEntity(movimiento);
            movimientoDTO.setNroBulto(String.valueOf(entity.getNroBulto()));
            dto.getMovimientoDTOs().add(movimientoDTO);
        }
    }

    private static void addAnalisisDTO(final LoteDTO dto, final Lote entity) {
        for (Analisis analisis : entity.getAnalisisList()) {
            dto.getAnalisisDTOs().add(DTOUtils.fromEntity(analisis));
        }
    }

}
