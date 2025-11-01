package com.mb.conitrack.utils;

import java.util.Objects;

import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.Bulto;
import com.mb.conitrack.entity.DetalleMovimiento;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.enums.MotivoEnum;
import com.mb.conitrack.enums.TipoMovimientoEnum;
import com.mb.conitrack.enums.UseCaseTag;

import static com.mb.conitrack.enums.MotivoEnum.RETIRO_MERCADO;
import static com.mb.conitrack.utils.MovimientoCommonUtils.formatObservacionesWithCU;
import static com.mb.conitrack.utils.MovimientoCommonUtils.generateMovimientoCode;
import static java.lang.Boolean.TRUE;

/**
 * Factory utility class for creating ALTA (inbound) movement entities.
 * <p>
 * This class handles the creation of Movimiento entities for operations that increase
 * stock levels, including:
 * <ul>
 *   <li>Purchase intake (COMPRA)</li>
 *   <li>Internal production (PRODUCCION_PROPIA)</li>
 *   <li>Sales returns (DEVOLUCION_VENTA)</li>
 *   <li>Market recalls (RETIRO_MERCADO - Alta variant)</li>
 * </ul>
 * </p>
 * <p>
 * All methods are static factory methods. Package-private helper methods are exposed
 * for unit testing while maintaining encapsulation.
 * </p>
 *
 * @see MovimientoBajaUtils
 * @see MovimientoModificacionUtils
 * @see MovimientoCommonUtils
 */
public class MovimientoAltaUtils {

    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private MovimientoAltaUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    //***********CU1 ALTA: COMPRA***********

    /**
     * Completes an ALTA movement by linking it to the lot and creating DetalleMovimiento
     * records for all bultos in the lot.
     * <p>
     * This method:
     * <ol>
     *   <li>Generates the movement code from lot timestamp</li>
     *   <li>Links the movement to the lot</li>
     *   <li>Creates a DetalleMovimiento for each bulto in the lot</li>
     * </ol>
     * </p>
     *
     * @param lote       the lot entity containing bultos (must not be null)
     * @param movimiento the movement to populate (modified in place, must not be null)
     * @throws NullPointerException if lote or movimiento is null
     */
    public static void addLoteInfoToMovimientoAlta(final Lote lote, final Movimiento movimiento) {
        Objects.requireNonNull(lote, "lote cannot be null");
        Objects.requireNonNull(movimiento, "movimiento cannot be null");
        movimiento.setCodigoMovimiento(generateMovimientoCode(lote.getCodigoLote(), lote.getFechaYHoraCreacion()));
        movimiento.setLote(lote);
        for (Bulto bulto : lote.getBultos()) {
            populateDetalleMovimientoAlta(movimiento, bulto);
        }
    }

    /**
     * Creates an ALTA movement for purchase intake (CU1 - Alta Ingreso Compra).
     * <p>
     * This is the initial movement created when a new lot is received from a supplier.
     * It records the entrada (inbound) of stock with tipo=ALTA and motivo=COMPRA.
     * </p>
     *
     * @param lote the newly created lot from purchase (must not be null)
     * @return configured ALTA movement with COMPRA motivo
     * @throws NullPointerException if lote is null
     */
    public static Movimiento createMovimientoAltaIngresoCompra(final Lote lote) {
        Objects.requireNonNull(lote, "lote cannot be null");
        Movimiento movimiento = createMovimientoAlta(lote);
        movimiento.setObservaciones(formatObservacionesWithCU(UseCaseTag.CU1, lote.getObservaciones()));
        movimiento.setMotivo(MotivoEnum.COMPRA);
        return movimiento;
    }

    //***********CU20 ALTA: PRODUCCION INTERNA***********

    /**
     * Creates an ALTA movement for internal production intake (CU20 - Alta Ingreso Producción).
     * <p>
     * This movement records the creation of a new lot from internal production processes.
     * It has tipo=ALTA and motivo=PRODUCCION_PROPIA.
     * </p>
     *
     * @param lote the newly created lot from production (must not be null)
     * @return configured ALTA movement with PRODUCCION_PROPIA motivo
     * @throws NullPointerException if lote is null
     */
    public static Movimiento createMovimientoAltaIngresoProduccion(final Lote lote) {
        Objects.requireNonNull(lote, "lote cannot be null");
        Movimiento movimiento = createMovimientoAlta(lote);
        movimiento.setObservaciones(formatObservacionesWithCU(UseCaseTag.CU20, lote.getObservaciones()));
        movimiento.setMotivo(MotivoEnum.PRODUCCION_PROPIA);
        return movimiento;
    }

    //***********CU23 ALTA: DEVOLUCION VENTA***********

    /**
     * Creates an ALTA movement for sales returns (CU23 - Alta Devolución Venta).
     * <p>
     * This movement records stock returning from customers, increasing available inventory.
     * It has tipo=ALTA and motivo=DEVOLUCION_VENTA.
     * </p>
     *
     * @param dto                the movement data transfer object with return information (must not be null)
     * @param loteAltaDevolucion the lot receiving the returned stock (must not be null)
     * @return configured ALTA movement with DEVOLUCION_VENTA motivo
     * @throws NullPointerException if dto or loteAltaDevolucion is null
     */
    public static Movimiento createMovimientoAltaDevolucion(final MovimientoDTO dto, final Lote loteAltaDevolucion) {
        Objects.requireNonNull(dto, "dto cannot be null");
        Objects.requireNonNull(loteAltaDevolucion, "loteAltaDevolucion cannot be null");
        Movimiento movimiento = createMovimientoAlta(dto, loteAltaDevolucion);
        movimiento.setObservaciones(formatObservacionesWithCU(UseCaseTag.CU23, dto.getObservaciones()));
        movimiento.setMotivo(MotivoEnum.DEVOLUCION_VENTA);
        return movimiento;
    }

    //***********CU24 ALTA: RECALL***********

    /**
     * Creates an ALTA movement for market recall returns (CU24 - Alta Retiro Mercado).
     * <p>
     * This movement records stock returning from the market due to a recall action.
     * It increases inventory but marks the stock for special handling.
     * It has tipo=ALTA and motivo=RETIRO_MERCADO.
     * </p>
     *
     * @param dto            the movement data transfer object with recall information (must not be null)
     * @param loteAltaRecall the lot receiving the recalled stock (must not be null)
     * @return configured ALTA movement with RETIRO_MERCADO motivo
     * @throws NullPointerException if dto or loteAltaRecall is null
     */
    public static Movimiento createMovimientoAltaRecall(final MovimientoDTO dto, final Lote loteAltaRecall) {
        Objects.requireNonNull(dto, "dto cannot be null");
        Objects.requireNonNull(loteAltaRecall, "loteAltaRecall cannot be null");
        Movimiento movimiento = createMovimientoAlta(dto, loteAltaRecall);
        movimiento.setObservaciones(formatObservacionesWithCU(UseCaseTag.CU24, dto.getObservaciones()));
        movimiento.setMotivo(RETIRO_MERCADO);
        return movimiento;
    }

    //*********************PACKAGE-PRIVATE HELPERS************************

    /**
     * Creates a base ALTA movement from a Lote entity.
     * <p>
     * This helper method initializes common fields for ALTA movements:
     * <ul>
     *   <li>tipo = ALTA</li>
     *   <li>fechaYHoraCreacion from lot</li>
     *   <li>fecha from lot creation</li>
     *   <li>cantidad = lot's initial quantity</li>
     *   <li>unidadMedida from lot</li>
     *   <li>dictamenFinal from lot</li>
     * </ul>
     * </p>
     *
     * @param lote the lot providing base information (must not be null)
     * @return partially configured ALTA movement (motivo and observations must be set by caller)
     * @throws NullPointerException if lote is null
     */
    static Movimiento createMovimientoAlta(final Lote lote) {
        Objects.requireNonNull(lote, "lote cannot be null");
        Movimiento movimiento = new Movimiento();
        movimiento.setTipoMovimiento(TipoMovimientoEnum.ALTA);
        movimiento.setFechaYHoraCreacion(lote.getFechaYHoraCreacion());
        movimiento.setCodigoMovimiento(generateMovimientoCode(lote.getCodigoLote(), lote.getFechaYHoraCreacion()));
        movimiento.setFecha(lote.getFechaYHoraCreacion().toLocalDate());
        movimiento.setCantidad(lote.getCantidadInicial());
        movimiento.setUnidadMedida(lote.getUnidadMedida());
        movimiento.setDictamenFinal(lote.getDictamen());
        movimiento.setLote(lote);
        movimiento.setActivo(true);
        return movimiento;
    }

    /**
     * Creates a base ALTA movement from a MovimientoDTO and Lote.
     * <p>
     * This overloaded helper is used for ALTA movements triggered by external events
     * (returns, recalls) rather than initial lot creation. It uses data from both
     * the DTO (user input) and the lot (existing entity).
     * </p>
     *
     * @param dto  the movement data transfer object with user input (must not be null)
     * @param lote the lot receiving the stock (must not be null)
     * @return partially configured ALTA movement (motivo and observations must be set by caller)
     * @throws NullPointerException if dto or lote is null
     */
    static Movimiento createMovimientoAlta(final MovimientoDTO dto, final Lote lote) {
        Objects.requireNonNull(dto, "dto cannot be null");
        Objects.requireNonNull(lote, "lote cannot be null");
        Movimiento movimiento = new Movimiento();
        movimiento.setTipoMovimiento(TipoMovimientoEnum.ALTA);
        movimiento.setFechaYHoraCreacion(dto.getFechaYHoraCreacion());
        movimiento.setCodigoMovimiento(generateMovimientoCode(lote.getCodigoLote(), dto.getFechaYHoraCreacion()));
        movimiento.setFecha(dto.getFechaMovimiento());
        movimiento.setCantidad(dto.getCantidad());
        movimiento.setUnidadMedida(dto.getUnidadMedida());
        movimiento.setDictamenInicial(dto.getDictamenInicial());
        movimiento.setDictamenFinal(dto.getDictamenFinal());
        movimiento.setLote(lote);
        movimiento.setActivo(true);
        return movimiento;
    }

    /**
     * Creates a DetalleMovimiento linking a movement to a bulto for ALTA operations.
     * <p>
     * For ALTA movements, the detail records the full initial quantity of the bulto
     * since we're adding the entire package to inventory. This establishes the
     * bidirectional relationship between Movimiento and Bulto through DetalleMovimiento.
     * </p>
     *
     * @param movimiento the movement to link (modified in place, must not be null)
     * @param bulto      the bulto to link (modified in place, must not be null)
     * @throws NullPointerException if movimiento or bulto is null
     */
    static void populateDetalleMovimientoAlta(final Movimiento movimiento, final Bulto bulto) {
        Objects.requireNonNull(movimiento, "movimiento cannot be null");
        Objects.requireNonNull(bulto, "bulto cannot be null");
        DetalleMovimiento det = DetalleMovimiento.builder()
                .movimiento(movimiento)
                .bulto(bulto)
                .cantidad(bulto.getCantidadInicial())
                .unidadMedida(bulto.getUnidadMedida())
                .activo(TRUE)
                .build();
        movimiento.getDetalles().add(det);
        bulto.getDetalles().add(det);
    }

}
