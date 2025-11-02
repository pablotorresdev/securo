package com.mb.conitrack.utils;

import java.util.Objects;

import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.Bulto;
import com.mb.conitrack.entity.DetalleMovimiento;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.entity.maestro.User;
import com.mb.conitrack.enums.MotivoEnum;
import com.mb.conitrack.enums.TipoMovimientoEnum;
import com.mb.conitrack.enums.UseCaseTag;

import static com.mb.conitrack.enums.MotivoEnum.RETIRO_MERCADO;
import static com.mb.conitrack.utils.MovimientoCommonUtils.formatObservacionesWithCU;
import static com.mb.conitrack.utils.MovimientoCommonUtils.generateMovimientoCode;
import static java.lang.Boolean.TRUE;

/** Factory para creación de movimientos ALTA (compra, producción, devoluciones, recall). */
public class MovimientoAltaUtils {

    private MovimientoAltaUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    //***********CU1 ALTA: COMPRA***********

    /** Completa movimiento ALTA enlazando lote y creando DetalleMovimiento por cada bulto. */
    public static void addLoteInfoToMovimientoAlta(final Lote lote, final Movimiento movimiento) {
        Objects.requireNonNull(lote, "lote cannot be null");
        Objects.requireNonNull(movimiento, "movimiento cannot be null");
        movimiento.setCodigoMovimiento(generateMovimientoCode(lote.getCodigoLote(), lote.getFechaYHoraCreacion()));
        movimiento.setLote(lote);
        for (Bulto bulto : lote.getBultos()) {
            populateDetalleMovimientoAlta(movimiento, bulto);
        }
    }

    /** CU1 - Crea movimiento ALTA por ingreso de compra (tipo=ALTA, motivo=COMPRA). */
    public static Movimiento createMovimientoAltaIngresoCompra(final Lote lote, final User creadoPor) {
        Objects.requireNonNull(lote, "lote cannot be null");
        Objects.requireNonNull(creadoPor, "creadoPor cannot be null");
        Movimiento movimiento = createMovimientoAlta(lote);
        movimiento.setObservaciones(formatObservacionesWithCU(UseCaseTag.CU1, lote.getObservaciones()));
        movimiento.setMotivo(MotivoEnum.COMPRA);
        movimiento.setCreadoPor(creadoPor);
        return movimiento;
    }

    //***********CU20 ALTA: PRODUCCION INTERNA***********

    /** CU20 - Crea movimiento ALTA por producción interna (tipo=ALTA, motivo=PRODUCCION_PROPIA). */
    public static Movimiento createMovimientoAltaIngresoProduccion(final Lote lote, final User creadoPor) {
        Objects.requireNonNull(lote, "lote cannot be null");
        Objects.requireNonNull(creadoPor, "creadoPor cannot be null");
        Movimiento movimiento = createMovimientoAlta(lote);
        movimiento.setObservaciones(formatObservacionesWithCU(UseCaseTag.CU20, lote.getObservaciones()));
        movimiento.setMotivo(MotivoEnum.PRODUCCION_PROPIA);
        movimiento.setCreadoPor(creadoPor);
        return movimiento;
    }

    //***********CU23 ALTA: DEVOLUCION VENTA***********

    /** CU23 - Crea movimiento ALTA por devolución de venta (tipo=ALTA, motivo=DEVOLUCION_VENTA). */
    public static Movimiento createMovimientoAltaDevolucion(final MovimientoDTO dto, final Lote loteAltaDevolucion, final User creadoPor) {
        Objects.requireNonNull(dto, "dto cannot be null");
        Objects.requireNonNull(loteAltaDevolucion, "loteAltaDevolucion cannot be null");
        Objects.requireNonNull(creadoPor, "creadoPor cannot be null");
        Movimiento movimiento = createMovimientoAlta(dto, loteAltaDevolucion);
        movimiento.setObservaciones(formatObservacionesWithCU(UseCaseTag.CU23, dto.getObservaciones()));
        movimiento.setMotivo(MotivoEnum.DEVOLUCION_VENTA);
        movimiento.setCreadoPor(creadoPor);
        return movimiento;
    }

    //***********CU24 ALTA: RECALL***********

    /** CU24 - Crea movimiento ALTA por retiro de mercado (tipo=ALTA, motivo=RETIRO_MERCADO). */
    public static Movimiento createMovimientoAltaRecall(final MovimientoDTO dto, final Lote loteAltaRecall, final User creadoPor) {
        Objects.requireNonNull(dto, "dto cannot be null");
        Objects.requireNonNull(loteAltaRecall, "loteAltaRecall cannot be null");
        Objects.requireNonNull(creadoPor, "creadoPor cannot be null");
        Movimiento movimiento = createMovimientoAlta(dto, loteAltaRecall);
        movimiento.setObservaciones(formatObservacionesWithCU(UseCaseTag.CU24, dto.getObservaciones()));
        movimiento.setMotivo(RETIRO_MERCADO);
        movimiento.setCreadoPor(creadoPor);
        return movimiento;
    }

    //*********************PACKAGE-PRIVATE HELPERS************************

    /** Crea movimiento ALTA base desde lote (inicializa campos comunes). */
    static Movimiento createMovimientoAlta(final Lote lote) {
        Objects.requireNonNull(lote, "lote cannot be null");
        Movimiento movimiento = new Movimiento();
        movimiento.setTipoMovimiento(TipoMovimientoEnum.ALTA);
        movimiento.setFechaYHoraCreacion(lote.getFechaYHoraCreacion());
        movimiento.setCodigoMovimiento(generateMovimientoCode(lote.getCodigoLote(), lote.getFechaYHoraCreacion()));
        movimiento.setFecha(lote.getFechaYHoraCreacion().toLocalDate());
        movimiento.setCantidad(lote.getCantidadInicial());
        movimiento.setUnidadMedida(lote.getUnidadMedida());
        movimiento.setDictamenInicial(lote.getDictamen());
        movimiento.setDictamenFinal(lote.getDictamen());
        movimiento.setLote(lote);
        movimiento.setActivo(true);
        return movimiento;
    }

    /** Crea movimiento ALTA desde DTO (para devoluciones y recall). */
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

    /** Crea DetalleMovimiento enlazando movimiento con bulto (registra cantidad inicial). */
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
