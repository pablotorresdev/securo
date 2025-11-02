package com.mb.conitrack.utils;

import java.util.Objects;

import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.entity.maestro.User;
import com.mb.conitrack.enums.DictamenEnum;
import com.mb.conitrack.enums.MotivoEnum;
import com.mb.conitrack.enums.TipoMovimientoEnum;
import com.mb.conitrack.enums.UseCaseTag;

import static com.mb.conitrack.enums.MotivoEnum.RETIRO_MERCADO;
import static com.mb.conitrack.utils.MovimientoCommonUtils.formatObservacionesWithCU;
import static com.mb.conitrack.utils.MovimientoCommonUtils.generateMovimientoCode;

/**
 * Factory utility class for creating MODIFICACION (modification) movement entities.
 * <p>
 * This class handles the creation of Movimiento entities for operations that modify
 * lot or movement data without directly affecting stock quantities, including:
 * <ul>
 *   <li>Analysis results recording (ANALISIS)</li>
 *   <li>Market recalls - modification variant (RETIRO_MERCADO)</li>
 *   <li>Movement reversals (REVERSO)</li>
 *   <li>Status changes and corrections</li>
 * </ul>
 * </p>
 * <p>
 * MODIFICACION movements typically change lot attributes (dictamen, estado) or
 * reverse previous movements without physically moving stock.
 * </p>
 * <p>
 * All methods are static factory methods. Package-private helper methods are exposed
 * for unit testing while maintaining encapsulation.
 * </p>
 *
 * @see MovimientoAltaUtils
 * @see MovimientoBajaUtils
 * @see MovimientoCommonUtils
 */
public class MovimientoModificacionUtils {

    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private MovimientoModificacionUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    //***********CU24 MODIF: RETIRO MERCADO***********

    /**
     * Creates a MODIFICACION movement for market recall status change
     * (CU24 - Modif Retiro Mercado).
     * <p>
     * This movement marks a lot as recalled from the market, setting dictamenFinal
     * to RETIRO_MERCADO. Unlike the ALTA variant which handles physical returns,
     * this MODIFICACION variant only changes the lot's status.
     * </p>
     *
     * @param dto the movement DTO with recall information
     * @param creadoPor the user who creates this movement
     * @return configured MODIFICACION movement with RETIRO_MERCADO motivo and dictamen
     */
    public static Movimiento createMovimientoModifRecall(final MovimientoDTO dto, final User creadoPor) {
        Objects.requireNonNull(creadoPor, "creadoPor cannot be null");
        Movimiento movimiento = new Movimiento();

        movimiento.setTipoMovimiento(TipoMovimientoEnum.MODIFICACION);
        movimiento.setMotivo(RETIRO_MERCADO);

        movimiento.setFechaYHoraCreacion(dto.getFechaYHoraCreacion());
        movimiento.setCodigoMovimiento(generateMovimientoCode(dto.getCodigoLote(), dto.getFechaYHoraCreacion()));
        movimiento.setDictamenFinal(DictamenEnum.RETIRO_MERCADO);

        movimiento.setFecha(dto.getFechaMovimiento());

        movimiento.setActivo(true);
        movimiento.setObservaciones(formatObservacionesWithCU(UseCaseTag.CU24, dto.getObservaciones()));
        movimiento.setCreadoPor(creadoPor);
        return movimiento;
    }

    /**
     * Creates a generic MODIFICACION movement for lot modifications.
     * <p>
     * This is a general-purpose factory for MODIFICACION movements used when
     * updating lot attributes such as:
     * <ul>
     *   <li>Analysis results (dictamen changes)</li>
     *   <li>Traceability updates</li>
     *   <li>Status corrections</li>
     *   <li>Re-analysis triggers</li>
     * </ul>
     * The motivo must be set by the caller based on the specific use case.
     * </p>
     *
     * @param dto the movement DTO with modification details
     * @param lote the lot being modified
     * @param creadoPor the user who creates this movement
     * @return partially configured MODIFICACION movement (motivo must be set by caller)
     */
    public static Movimiento createMovimientoModificacion(final MovimientoDTO dto, final Lote lote, final User creadoPor) {
        Objects.requireNonNull(creadoPor, "creadoPor cannot be null");
        Movimiento movimiento = new Movimiento();
        movimiento.setTipoMovimiento(TipoMovimientoEnum.MODIFICACION);
        movimiento.setFechaYHoraCreacion(dto.getFechaYHoraCreacion());
        movimiento.setFecha(dto.getFechaMovimiento());
        movimiento.setCodigoMovimiento(generateMovimientoCode(lote.getCodigoLote(), dto.getFechaYHoraCreacion()));

        movimiento.setObservaciones(dto.getObservaciones());
        movimiento.setLote(lote);
        movimiento.setActivo(true);
        movimiento.setCreadoPor(creadoPor);

        return movimiento;
    }

    /**
     * Creates a MODIFICACION movement to reverse a previous movement.
     * <p>
     * Reversal movements (REVERSO) undo the effects of a previous movement, typically
     * used for error corrections. The reversal links to the original movement via
     * movimientoOrigen, creating an audit trail.
     * </p>
     * <p>
     * Example use cases:
     * <ul>
     *   <li>Reversing an incorrect sale</li>
     *   <li>Canceling a production consumption</li>
     *   <li>Undoing a stock adjustment</li>
     * </ul>
     * </p>
     *
     * @param dto the movement DTO with reversal details
     * @param movimientoOrigen the original movement being reversed
     * @param creadoPor the user who creates this reversal movement
     * @return configured MODIFICACION movement with REVERSO motivo, linked to original
     */
    public static Movimiento createMovimientoReverso(final MovimientoDTO dto, final Movimiento movimientoOrigen, final User creadoPor) {
        Objects.requireNonNull(creadoPor, "creadoPor cannot be null");
        Movimiento movimiento = createMovimientoModificacion(dto, movimientoOrigen.getLote(), creadoPor);
        movimiento.setFecha(dto.getFechaMovimiento());
        movimiento.setMovimientoOrigen(movimientoOrigen);
        movimiento.setMotivo(MotivoEnum.REVERSO);
        return movimiento;
    }

}
