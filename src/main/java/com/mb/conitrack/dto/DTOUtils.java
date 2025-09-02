package com.mb.conitrack.dto;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.thymeleaf.util.StringUtils;

import com.mb.conitrack.entity.Analisis;
import com.mb.conitrack.entity.Bulto;
import com.mb.conitrack.entity.DetalleMovimiento;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.entity.Traza;
import com.mb.conitrack.entity.maestro.Producto;
import com.mb.conitrack.enums.TipoProductoEnum;

public class DTOUtils {

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

    public static List<AnalisisDTO> fromAnalisisEntities(final List<Analisis> analisisList) {
        final List<AnalisisDTO> analisisDtos = new ArrayList<>();
        for (Analisis entity : analisisList) {
            analisisDtos.add(DTOUtils.fromAnalisisEntity(entity));
        }
        return analisisDtos;
    }

    public static AnalisisDTO fromAnalisisEntity(Analisis entity) {
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

    public static List<BultoDTO> fromBultoEntities(final List<Bulto> bultoList) {
        final List<BultoDTO> bultoDTOs = new ArrayList<>();
        for (Bulto entity : bultoList) {
            bultoDTOs.add(DTOUtils.fromBultoEntity(entity));
        }
        return bultoDTOs;
    }

    public static BultoDTO fromBultoEntity(Bulto entity) {
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
            dto.getDetalleMovimientoDTOs().add(fromDetalleMovimientoEntity(detalleMovimiento));
        }

        return dto;
    }

    public static DetalleMovimientoDTO fromDetalleMovimientoEntity(DetalleMovimiento entity) {
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

    public static List<LoteDTO> fromLoteEntities(final List<Lote> loteList) {
        final List<LoteDTO> lotesDtos = new ArrayList<>();
        for (Lote entity : loteList) {
            lotesDtos.add(DTOUtils.fromLoteEntity(entity));
        }
        return lotesDtos;
    }

    public static LoteDTO fromLoteEntity(Lote loteEntity) {
        if (loteEntity == null) {
            return null;
        }

        LoteDTO loteDTO = new LoteDTO();

        loteDTO.setFechaYHoraCreacion(loteEntity.getFechaYHoraCreacion());

        addInfoProducto(loteEntity, loteDTO);
        addInfoProveedor(loteEntity, loteDTO);
        addInfoLote(loteEntity, loteDTO);
        addBultoDTOs(loteEntity, loteDTO);
        addMovimientoDTOs(loteEntity, loteDTO);
        addAnalisisDTOs(loteEntity, loteDTO);
        addTrazaDTOs(loteEntity, loteDTO);

        Long trazaInicial = null;
        if (loteEntity.getFirstActiveTraza() != null) {
            trazaInicial = loteEntity.getFirstActiveTraza().getNroTraza();
        }

        for (int i = 1; i <= loteEntity.getBultos().size(); i++) {
            final Bulto bultoEntity = loteEntity.getBultoByNro(i);
            if (bultoEntity==null || !bultoEntity.getActivo()) {
                continue;
            }

            if (bultoEntity.getFirstActiveTraza() != null) {
                if (trazaInicial == null) {
                    trazaInicial = bultoEntity.getFirstActiveTraza().getNroTraza();
                } else {
                    trazaInicial = Math.min(trazaInicial, bultoEntity.getFirstActiveTraza().getNroTraza());
                }
            }

            final int index = loteDTO.getCantidadesBultos().size();
            loteDTO.getNroBultoList().add(index, bultoEntity.getNroBulto());
            loteDTO.getCantidadesBultos().add(index, bultoEntity.getCantidadActual());
            loteDTO.getUnidadMedidaBultos().add(index, bultoEntity.getUnidadMedida());
        }

        loteDTO.setEstado(loteEntity.getEstado());
        loteDTO.setTrazaInicial(trazaInicial);
        loteDTO.setCantidadInicial(loteEntity.getCantidadInicial());
        loteDTO.setCantidadActual(loteEntity.getCantidadActual());
        loteDTO.setUnidadMedida(loteEntity.getUnidadMedida());

        loteDTO.getBultosDTOs().sort(Comparator.comparing(BultoDTO::getNroBulto));
        loteDTO.getAnalisisDTOs().sort(Comparator.comparing(AnalisisDTO::getNroAnalisis));
        loteDTO.getMovimientoDTOs().sort(Comparator.comparing(MovimientoDTO::getFechaMovimiento));
        loteDTO.getTrazaDTOs().sort(Comparator.comparing(TrazaDTO::getNroTraza));
        return loteDTO;
    }

    public static List<MovimientoDTO> fromMovimientoEntities(final List<Movimiento> movimientoList) {
        final List<MovimientoDTO> movimientoDtos = new ArrayList<>();
        for (Movimiento entity : movimientoList) {
            movimientoDtos.add(DTOUtils.fromMovimientoEntity(entity));
        }
        return movimientoDtos;
    }

    public static MovimientoDTO fromMovimientoEntity(Movimiento entity) {
        if (entity == null) {
            return null;
        }

        MovimientoDTO dto = new MovimientoDTO();
        dto.setFechaMovimiento(entity.getFecha());
        dto.setCodigoMovimiento(entity.getCodigoMovimiento());
        if (entity.getLote() != null) {
            dto.setCodigoLote(entity.getLote().getCodigoLote());
            dto.setLoteId(entity.getLote().getId());
            for (Bulto bulto : entity.getLote().getBultos()) {
                for (DetalleMovimiento detalleMovimiento : bulto.getDetalles()) {
                    dto.getDetalleMovimientoDTOs().add(fromDetalleMovimientoEntity(detalleMovimiento));
                }
            }
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

    public static List<TrazaDTO> fromTrazaEntities(final List<Traza> trazaList) {
        final List<TrazaDTO> trazaDTOs = new ArrayList<>();
        for (Traza entity : trazaList) {
            trazaDTOs.add(DTOUtils.fromTrazaEntity(entity));
        }
        return trazaDTOs;
    }

    public static TrazaDTO fromTrazaEntity(Traza entity) {
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

    static void addAnalisisDTOs(final Lote entity, final LoteDTO loteDTO) {
        for (Analisis analisis : entity.getAnalisisList()) {
            if (analisis.getActivo()) {
                loteDTO.getAnalisisDTOs().add(DTOUtils.fromAnalisisEntity(analisis));
            }
        }
    }

    static void addBultoDTOs(final Lote entity, final LoteDTO loteDTO) {
        for (Bulto bulto : entity.getBultos()) {
            if (bulto.getActivo()) {
                final BultoDTO bultoDto = DTOUtils.fromBultoEntity(bulto);
                loteDTO.getBultosDTOs().add(bultoDto);
            }
        }
    }

    static void addInfoLote(final Lote entity, final LoteDTO loteDTO) {
        loteDTO.setCodigoLote(entity.getCodigoLote());
        loteDTO.setFechaIngreso(entity.getFechaIngreso());
        loteDTO.setBultosTotales(entity.getBultosTotales());
        loteDTO.setEstado(entity.getEstado());
        loteDTO.setDictamen(entity.getDictamen());
        loteDTO.setObservaciones(entity.getObservaciones());
        loteDTO.setOrdenProduccion(entity.getOrdenProduccionOrigen());
    }

    static void addInfoProducto(final Lote loteEntity, final LoteDTO loteDTO) {
        if (loteEntity.getProducto() != null) {
            final Producto producto = loteEntity.getProducto();
            loteDTO.setProductoId(producto.getId());
            loteDTO.setNombreProducto(producto.getNombreGenerico());
            loteDTO.setCodigoProducto(producto.getCodigoProducto());
            loteDTO.setTipoProducto(producto.getTipoProducto());
            loteDTO.setProductoDestino(producto.getProductoDestino() != null ? producto.getProductoDestino() : null);
        }
    }

    static void addInfoProveedor(final Lote bultoEntity, final LoteDTO loteDTO) {
        loteDTO.setProveedorId(bultoEntity.getProveedor() != null ? bultoEntity.getProveedor().getId() : null);
        loteDTO.setNombreProveedor(bultoEntity.getProveedor() != null
            ? bultoEntity.getProveedor().getRazonSocial()
            : null);
        loteDTO.setFabricanteId(bultoEntity.getFabricante() != null ? bultoEntity.getFabricante().getId() : null);
        loteDTO.setNombreFabricante(bultoEntity.getFabricante() != null
            ? bultoEntity.getFabricante().getRazonSocial()
            : null);
        loteDTO.setLoteProveedor(bultoEntity.getLoteProveedor());
        loteDTO.setPaisOrigen(bultoEntity.getPaisOrigen());
        loteDTO.setFechaReanalisisProveedor(bultoEntity.getFechaReanalisisProveedor());
        loteDTO.setFechaVencimientoProveedor(bultoEntity.getFechaVencimientoProveedor());
        loteDTO.setNroRemito(bultoEntity.getNroRemito());
        loteDTO.setDetalleConservacion(bultoEntity.getDetalleConservacion());
    }

    static void addMovimientoDTOs(final Lote entity, final LoteDTO loteDTO) {
        for (Movimiento movimiento : entity.getMovimientos()) {
            if (movimiento.getActivo()) {
                final MovimientoDTO movimientoDTO = DTOUtils.fromMovimientoEntity(movimiento);
                loteDTO.getMovimientoDTOs().add(movimientoDTO);
            }
        }
    }

    static void addTrazaDTOs(final Lote entity, final LoteDTO loteDTO) {
        for (Traza traza : entity.getTrazas()) {
            if (traza.getActivo()) {
                loteDTO.getTrazaDTOs().add(DTOUtils.fromTrazaEntity(traza));
            }
        }
    }

}
