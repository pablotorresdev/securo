package com.mb.conitrack.dto;

import java.util.List;

import com.mb.conitrack.dto.mapper.*;
import com.mb.conitrack.entity.Analisis;
import com.mb.conitrack.entity.Bulto;
import com.mb.conitrack.entity.DetalleMovimiento;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.entity.Traza;

/**
 * Clase de utilidades para conversión entre entidades y DTOs.
 * Actúa como fachada que delega a mappers especializados:
 * - AnalisisMapper: Conversiones de Analisis
 * - BultoMapper: Conversiones de Bulto
 * - DetalleMovimientoMapper: Conversiones de DetalleMovimiento
 * - LoteMapper: Conversiones de Lote
 * - MovimientoMapper: Conversiones de Movimiento
 * - TrazaMapper: Conversiones de Traza
 *
 * @deprecated Se recomienda usar directamente los mappers especializados.
 * Esta clase se mantiene por compatibilidad hacia atrás.
 */
public class DTOUtils {

    // ========== Analisis ==========

    public static Analisis createAnalisis(final MovimientoDTO dto) {
        return AnalisisMapper.createAnalisis(dto);
    }

    public static List<AnalisisDTO> fromAnalisisEntities(final List<Analisis> analisisList) {
        return AnalisisMapper.fromEntities(analisisList);
    }

    public static AnalisisDTO fromAnalisisEntity(Analisis entity) {
        return AnalisisMapper.fromEntity(entity);
    }

    // ========== Bulto ==========

    public static List<BultoDTO> fromBultoEntities(final List<Bulto> bultoList) {
        return BultoMapper.fromEntities(bultoList);
    }

    public static BultoDTO fromBultoEntity(Bulto entity) {
        return BultoMapper.fromEntity(entity);
    }

    // ========== DetalleMovimiento ==========

    public static DetalleMovimientoDTO fromDetalleMovimientoEntity(DetalleMovimiento entity) {
        return DetalleMovimientoMapper.fromEntity(entity);
    }

    // ========== Lote ==========

    public static List<LoteDTO> fromLoteEntities(final List<Lote> loteList) {
        return LoteMapper.fromEntities(loteList);
    }

    public static LoteDTO fromLoteEntity(Lote loteEntity) {
        return LoteMapper.fromEntity(loteEntity);
    }

    // ========== Movimiento ==========

    public static List<MovimientoDTO> fromMovimientoEntities(final List<Movimiento> movimientoList) {
        return MovimientoMapper.fromEntities(movimientoList);
    }

    public static MovimientoDTO fromMovimientoEntity(Movimiento entity) {
        return MovimientoMapper.fromEntity(entity);
    }

    // ========== Traza ==========

    public static List<TrazaDTO> fromTrazaEntities(final List<Traza> trazaList) {
        return TrazaMapper.fromEntities(trazaList);
    }

    public static TrazaDTO fromTrazaEntity(Traza entity) {
        return TrazaMapper.fromEntity(entity);
    }
}
