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
        dto.setFechaYHoraCreacion(entity.getFechaYHoraCreacion());
        dto.setNroAnalisis(entity.getNroAnalisis());
        dto.setFechaRealizado(entity.getFechaRealizado());
        dto.setFechaReanalisis(entity.getFechaReanalisis());
        dto.setFechaVencimiento(entity.getFechaVencimiento());
        dto.setDictamen(entity.getDictamen());
        dto.setTitulo(entity.getTitulo());
        dto.setObservaciones(entity.getObservaciones());
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
        }

        dto.setCantidad(entity.getCantidad());
        dto.setUnidadMedida(entity.getUnidadMedida());

        dto.setObservaciones(entity.getObservaciones());
        dto.setNroAnalisis(entity.getNroAnalisis());
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
        final String nroAnalisis = StringUtils.isEmpty(dto.getNroReanalisis()) ? dto.getNroAnalisis() : dto.getNroReanalisis();
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


    public static LoteDTO fromEntities(List<Lote> entities) {
        if (entities == null || entities.isEmpty()) {
            return null;
        }

        LoteDTO dto = new LoteDTO();
        boolean firstCase = true;
        for (Lote entity : entities) {
            if (firstCase) {
                dto.setFechaYHoraCreacion(entity.getFechaYHoraCreacion());

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

                dto.setFabricanteId(entity.getFabricante() != null ? entity.getFabricante().getId() : null);
                dto.setNombreFabricante(entity.getFabricante() != null ? entity.getFabricante().getRazonSocial() : null);

                dto.setCodigoInterno(entity.getCodigoInterno());
                dto.setPaisOrigen(entity.getPaisOrigen());

                dto.setFechaIngreso(entity.getFechaIngreso());

                dto.setBultosTotales(entity.getBultosTotales());

                dto.setCantidadInicial(entity.getCantidadInicial());
                dto.setUnidadMedida(entity.getUnidadMedida());

                dto.setLoteProveedor(entity.getLoteProveedor());

                dto.setFechaReanalisisProveedor(entity.getFechaReanalisisProveedor());
                dto.setFechaVencimientoProveedor(entity.getFechaVencimientoProveedor());
                dto.setEstadoLote(entity.getEstadoLote().getValor());
                dto.setDictamen(entity.getDictamen());
                dto.setLoteOrigenId(entity.getLoteOrigen()!=null ? entity.getLoteOrigen().getId() : null);

                dto.setNroRemito(entity.getNroRemito());
                dto.setDetalleConservacion(entity.getDetalleConservacion());
                dto.setObservaciones(entity.getObservaciones());

                dto.getCantidadesBultos().add(entity.getCantidadInicial());
                dto.getUnidadMedidaBultos().add(entity.getUnidadMedida());

                addMovimientosDTO(dto, entity);
                addAnalisisDTO(dto, entity);

                firstCase = false;
            } else {
                //TODO: Hacer suma por bulto
                //dto.getCantidadInicial().add(entity.getCantidadInicial().from(entity.getUnidadMedida()).to(dto.getUnidadMedida()))

                dto.getCantidadesBultos().add(entity.getNroBulto() - 1, entity.getCantidadInicial());
                dto.getUnidadMedidaBultos().add(entity.getNroBulto() - 1, entity.getUnidadMedida());

                addMovimientosDTO(dto, entity);
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
