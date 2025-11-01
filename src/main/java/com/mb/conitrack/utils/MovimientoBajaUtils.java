package com.mb.conitrack.utils;

import java.util.Objects;

import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.Analisis;
import com.mb.conitrack.entity.Bulto;
import com.mb.conitrack.entity.DetalleMovimiento;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.enums.DictamenEnum;
import com.mb.conitrack.enums.MotivoEnum;
import com.mb.conitrack.enums.TipoMovimientoEnum;
import com.mb.conitrack.enums.UseCaseTag;

import static com.mb.conitrack.enums.MotivoEnum.*;
import static com.mb.conitrack.utils.MovimientoCommonUtils.*;
import static java.lang.Boolean.TRUE;

/**
 * Factory utility class for creating BAJA (outbound) movement entities.
 * <p>
 * This class handles the creation of Movimiento entities for operations that decrease
 * stock levels, including:
 * <ul>
 *   <li>Sales (VENTA)</li>
 *   <li>Production consumption (CONSUMO_PRODUCCION)</li>
 *   <li>Sampling (MUESTREO)</li>
 *   <li>Return to supplier (DEVOLUCION_COMPRA)</li>
 *   <li>Stock adjustments (AJUSTE)</li>
 * </ul>
 * </p>
 * <p>
 * All methods are static factory methods. Package-private helper methods are exposed
 * for unit testing while maintaining encapsulation.
 * </p>
 *
 * @see MovimientoAltaUtils
 * @see MovimientoModificacionUtils
 * @see MovimientoCommonUtils
 */
public class MovimientoBajaUtils {

    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private MovimientoBajaUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    //***********CU4 BAJA: DEVOLUCION COMPRA***********

    /**
     * Creates a BAJA movement for returning defective stock to supplier
     * (CU4 - Baja Devolución Compra).
     * <p>
     * This movement records stock being returned to the supplier, typically due to
     * quality issues. It has tipo=BAJA, motivo=DEVOLUCION_COMPRA, and sets
     * dictamenFinal=RECHAZADO to indicate the rejection reason.
     * </p>
     *
     * @param dto the movement data transfer object with return information
     * @return configured BAJA movement with DEVOLUCION_COMPRA motivo and RECHAZADO dictamen
     */
    public static Movimiento createMovimientoDevolucionCompra(final MovimientoDTO dto) {
        Movimiento movimiento = new Movimiento();

        movimiento.setTipoMovimiento(TipoMovimientoEnum.BAJA);
        movimiento.setMotivo(DEVOLUCION_COMPRA);

        movimiento.setFechaYHoraCreacion(dto.getFechaYHoraCreacion());
        movimiento.setCodigoMovimiento(generateMovimientoCode(dto.getCodigoLote(), dto.getFechaYHoraCreacion()));
        movimiento.setDictamenFinal(DictamenEnum.RECHAZADO);

        movimiento.setFecha(dto.getFechaMovimiento());

        movimiento.setActivo(true);
        movimiento.setObservaciones(formatObservacionesWithCU(UseCaseTag.CU4, dto.getObservaciones()));
        return movimiento;
    }

    //***********CU7 BAJA: CONSUMO PRODUCCION***********

    /**
     * Creates a BAJA movement for production consumption (CU7 - Baja Consumo Producción).
     * <p>
     * This movement records raw materials or ingredients being consumed in production
     * processes. It has tipo=BAJA and motivo=CONSUMO_PRODUCCION. The consumed quantity
     * reduces available stock.
     * </p>
     *
     * @param dto the lot DTO containing consumption details
     * @param lote the lot being consumed
     * @return configured BAJA movement with CONSUMO_PRODUCCION motivo
     */
    public static Movimiento createMovimientoBajaProduccion(final LoteDTO dto, final Lote lote) {
        Movimiento movimiento = new Movimiento();
        movimiento.setTipoMovimiento(TipoMovimientoEnum.BAJA);
        movimiento.setMotivo(MotivoEnum.CONSUMO_PRODUCCION);
        movimiento.setFechaYHoraCreacion(dto.getFechaYHoraCreacion());
        movimiento.setCodigoMovimiento(generateMovimientoCode(lote.getCodigoLote(), dto.getFechaYHoraCreacion()));
        movimiento.setFecha(dto.getFechaEgreso());
        movimiento.setObservaciones(formatObservacionesWithCU(UseCaseTag.CU7, dto.getObservaciones()));
        movimiento.setLote(lote);
        movimiento.setActivo(true);

        return movimiento;
    }

    //***********CU22 BAJA: VENTA***********

    /**
     * Creates a BAJA movement for sales (CU22 - Baja Venta).
     * <p>
     * This movement records finished products being sold to customers, reducing
     * available inventory. It has tipo=BAJA and motivo=VENTA.
     * </p>
     *
     * @param dto the lot DTO containing sales details
     * @param lote the lot being sold
     * @return configured BAJA movement with VENTA motivo
     */
    public static Movimiento createMovimientoBajaVenta(final LoteDTO dto, final Lote lote) {
        Movimiento movimiento = new Movimiento();
        movimiento.setTipoMovimiento(TipoMovimientoEnum.BAJA);
        movimiento.setMotivo(MotivoEnum.VENTA);
        movimiento.setFechaYHoraCreacion(dto.getFechaYHoraCreacion());
        movimiento.setCodigoMovimiento(generateMovimientoCode(lote.getCodigoLote(), dto.getFechaYHoraCreacion()));
        movimiento.setFecha(dto.getFechaEgreso());
        movimiento.setObservaciones(formatObservacionesWithCU(UseCaseTag.CU22, dto.getObservaciones()));
        movimiento.setLote(lote);
        movimiento.setActivo(true);

        return movimiento;
    }

    //***********CU25 BAJA: AJUSTE STOCK***********

    /**
     * Creates a BAJA movement for stock adjustments (CU25 - Baja Ajuste Stock).
     * <p>
     * This movement records inventory corrections for a specific bulto, typically due to
     * discrepancies found during physical counts, damage, or loss. It has tipo=BAJA
     * and motivo=AJUSTE.
     * </p>
     * <p>
     * Unlike other movements, adjustments are bulto-specific and generate codes
     * including the bulto number.
     * </p>
     *
     * @param dto the movement DTO with adjustment details
     * @param bulto the specific bulto being adjusted
     * @return configured BAJA movement with AJUSTE motivo
     */
    public static Movimiento createMovimientoAjusteStock(
        final MovimientoDTO dto,
        final Bulto bulto) {

        Movimiento movimiento = new Movimiento();

        movimiento.setTipoMovimiento(TipoMovimientoEnum.BAJA);
        movimiento.setMotivo(AJUSTE);

        movimiento.setFechaYHoraCreacion(dto.getFechaYHoraCreacion());
        movimiento.setFecha(dto.getFechaMovimiento());
        movimiento.setCantidad(dto.getCantidad());
        movimiento.setUnidadMedida(dto.getUnidadMedida());
        movimiento.setNroAnalisis(dto.getNroAnalisis());
        movimiento.setActivo(true);

        populateDetalleMovimiento(movimiento, bulto);
        movimiento.setCodigoMovimiento(generateMovimientoCodeForBulto(
            bulto.getLote().getCodigoLote(),
            bulto.getNroBulto(),
            dto.getFechaYHoraCreacion()));

        movimiento.setObservaciones(formatObservacionesWithCU(UseCaseTag.CU25, dto.getObservaciones()));

        movimiento.setLote(bulto.getLote());
        return movimiento;
    }

    //***********CU3 BAJA: MUESTREO***********

    /**
     * Creates a BAJA movement for sampling with analysis tracking (CU3 - Baja Muestreo).
     * <p>
     * This movement records samples taken from a specific bulto for quality analysis.
     * It has tipo=BAJA and motivo=MUESTREO, and links to the analysis record via nroAnalisis.
     * The sampled quantity reduces available stock.
     * </p>
     *
     * @param dto the movement DTO with sampling details
     * @param bulto the bulto being sampled
     * @param ultimoAnalisis the analysis record associated with this sample
     * @return configured BAJA movement with MUESTREO motivo and linked analysis
     */
    public static Movimiento createMovimientoMuestreoConAnalisis(
        final MovimientoDTO dto,
        final Bulto bulto,
        final Analisis ultimoAnalisis) {
        final Movimiento movimiento = createMovimientoPorMuestreo(dto);
        populateDetalleMovimiento(movimiento, bulto);
        movimiento.setCodigoMovimiento(generateMovimientoCodeForBulto(
            bulto.getLote().getCodigoLote(),
            bulto.getNroBulto(),
            dto.getFechaYHoraCreacion()));
        movimiento.setLote(bulto.getLote());
        movimiento.setNroAnalisis(ultimoAnalisis.getNroAnalisis());
        return movimiento;
    }

    /**
     * Creates a BAJA movement for multi-bulto sampling (CU3 variant - Baja Muestreo).
     * <p>
     * This variant is used when sampling affects multiple bultos from the same lot.
     * Unlike single-bulto sampling, this creates a lot-level movement without
     * bulto-specific details in the code.
     * </p>
     *
     * @param dto the lot DTO with sampling details
     * @param lote the lot being sampled
     * @return configured BAJA movement with MUESTREO motivo
     */
    public static Movimiento createMovimientoPorMuestreoMultiBulto(final LoteDTO dto, final Lote lote) {
        Movimiento movimiento = new Movimiento();

        movimiento.setTipoMovimiento(TipoMovimientoEnum.BAJA);
        movimiento.setMotivo(MUESTREO);

        movimiento.setFechaYHoraCreacion(dto.getFechaYHoraCreacion());

        movimiento.setCodigoMovimiento(generateMovimientoCode(lote.getCodigoLote(), dto.getFechaYHoraCreacion()));

        movimiento.setFecha(dto.getFechaEgreso());

        movimiento.setObservaciones(formatObservacionesWithCU(UseCaseTag.CU3, dto.getObservaciones()));

        movimiento.setLote(lote);
        movimiento.setActivo(true);

        return movimiento;
    }

    //*********************PACKAGE-PRIVATE HELPERS************************

    /**
     * Creates a base BAJA movement for sampling operations.
     * <p>
     * This helper method initializes common fields for sampling movements:
     * <ul>
     *   <li>tipo = BAJA</li>
     *   <li>motivo = MUESTREO</li>
     *   <li>fecha, cantidad, unidadMedida from DTO</li>
     *   <li>nroAnalisis for traceability</li>
     * </ul>
     * </p>
     *
     * @param dto the movement DTO with sampling details
     * @return partially configured BAJA movement (code and lote must be set by caller)
     */
    static Movimiento createMovimientoPorMuestreo(final MovimientoDTO dto) {
        Movimiento movimiento = new Movimiento();

        movimiento.setTipoMovimiento(TipoMovimientoEnum.BAJA);
        movimiento.setMotivo(MUESTREO);

        movimiento.setFechaYHoraCreacion(dto.getFechaYHoraCreacion());
        movimiento.setFecha(dto.getFechaMovimiento());
        movimiento.setCantidad(dto.getCantidad());
        movimiento.setUnidadMedida(dto.getUnidadMedida());
        movimiento.setNroAnalisis(dto.getNroAnalisis());
        movimiento.setActivo(true);

        movimiento.setObservaciones(formatObservacionesWithCU(UseCaseTag.CU3, dto.getObservaciones()));
        return movimiento;
    }

    /**
     * Creates a DetalleMovimiento linking a movement to a bulto for BAJA operations.
     * <p>
     * For BAJA movements, the detail records the actual quantity being removed
     * (from the movement), not the bulto's full quantity. This establishes the
     * bidirectional relationship between Movimiento and Bulto through DetalleMovimiento.
     * </p>
     *
     * @param movimiento the movement to link (modified in place)
     * @param bulto the bulto to link (modified in place)
     */
    static void populateDetalleMovimiento(final Movimiento movimiento, final Bulto bulto) {
        DetalleMovimiento det = DetalleMovimiento.builder()
            .movimiento(movimiento)
            .bulto(bulto)
            .cantidad(movimiento.getCantidad())
            .unidadMedida(movimiento.getUnidadMedida())
            .activo(TRUE)
            .build();
        movimiento.getDetalles().add(det);
        bulto.getDetalles().add(det);
    }

}
