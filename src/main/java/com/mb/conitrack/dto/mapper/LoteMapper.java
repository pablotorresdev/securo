package com.mb.conitrack.dto.mapper;

import com.mb.conitrack.dto.BultoDTO;
import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.entity.Analisis;
import com.mb.conitrack.entity.Bulto;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.entity.Traza;
import com.mb.conitrack.entity.maestro.Producto;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static java.lang.Boolean.TRUE;

/**
 * Mapper para conversiones entre entidad Lote y LoteDTO.
 * Este es el mapper más complejo ya que agrega información de múltiples entidades relacionadas.
 */
public class LoteMapper {

    /**
     * Convierte una lista de entidades Lote a DTOs.
     */
    public static List<LoteDTO> fromEntities(final List<Lote> loteList) {
        final List<LoteDTO> lotesDtos = new ArrayList<>();
        for (Lote entity : loteList) {
            lotesDtos.add(fromEntity(entity));
        }
        return lotesDtos;
    }

    /**
     * Convierte una entidad Lote a DTO con toda su información relacionada.
     */
    public static LoteDTO fromEntity(Lote loteEntity) {
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
            if (bultoEntity == null || !TRUE.equals(bultoEntity.getActivo())) {
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
        loteDTO.getAnalisisDTOs().sort(Comparator.comparing(dto -> dto.getNroAnalisis()));
        loteDTO.getMovimientoDTOs().sort(Comparator.comparing(dto -> dto.getFechaMovimiento()));
        loteDTO.getTrazaDTOs().sort(Comparator.comparing(dto -> dto.getNroTraza()));
        return loteDTO;
    }

    /**
     * Agrega análisis del lote y del lote origen (si existe) al DTO.
     */
    static void addAnalisisDTOs(final Lote entity, final LoteDTO loteDTO) {
        for (Analisis analisis : entity.getAnalisisList()) {
            if (TRUE.equals(analisis.getActivo())) {
                loteDTO.getAnalisisDTOs().add(AnalisisMapper.fromEntity(analisis));
            }
        }
        if (entity.getLoteOrigen() != null) {
            for (Analisis analisis : entity.getLoteOrigen().getAnalisisList()) {
                if (TRUE.equals(analisis.getActivo())) {
                    loteDTO.getAnalisisDTOs().add(AnalisisMapper.fromEntity(analisis));
                }
            }
        }
    }

    /**
     * Agrega bultos activos del lote al DTO.
     */
    static void addBultoDTOs(final Lote entity, final LoteDTO loteDTO) {
        for (Bulto bulto : entity.getBultos()) {
            if (TRUE.equals(bulto.getActivo())) {
                final BultoDTO bultoDto = BultoMapper.fromEntity(bulto);
                loteDTO.getBultosDTOs().add(bultoDto);
            }
        }
    }

    /**
     * Agrega información básica del lote al DTO.
     */
    static void addInfoLote(final Lote entity, final LoteDTO loteDTO) {
        loteDTO.setCodigoLote(entity.getCodigoLote());
        loteDTO.setFechaIngreso(entity.getFechaIngreso());
        loteDTO.setBultosTotales(entity.getBultosTotales());
        loteDTO.setEstado(entity.getEstado());
        loteDTO.setDictamen(entity.getDictamen());
        loteDTO.setObservaciones(entity.getObservaciones());
        loteDTO.setOrdenProduccion(entity.getOrdenProduccionOrigen());
    }

    /**
     * Agrega información del producto al DTO.
     */
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

    /**
     * Agrega información del proveedor y fabricante al DTO.
     */
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

    /**
     * Agrega movimientos activos del lote al DTO.
     */
    static void addMovimientoDTOs(final Lote entity, final LoteDTO loteDTO) {
        for (Movimiento movimiento : entity.getMovimientos()) {
            if (TRUE.equals(movimiento.getActivo())) {
                loteDTO.getMovimientoDTOs().add(MovimientoMapper.fromEntity(movimiento));
            }
        }
    }

    /**
     * Agrega trazas activas del lote al DTO.
     */
    static void addTrazaDTOs(final Lote entity, final LoteDTO loteDTO) {
        loteDTO.setTrazado(entity.getTrazado() != null && entity.getTrazado());
        for (Traza traza : entity.getActiveTrazas()) {
            loteDTO.getTrazaDTOs().add(TrazaMapper.fromEntity(traza));
        }
    }
}
