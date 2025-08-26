package com.mb.conitrack.utils;

import java.time.format.DateTimeFormatter;

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

import static com.mb.conitrack.enums.MotivoEnum.DEVOLUCION_COMPRA;
import static com.mb.conitrack.enums.MotivoEnum.MUESTREO;
import static com.mb.conitrack.enums.MotivoEnum.RETIRO_MERCADO;

public class MovimientoEntityUtils {

    //***********CU1 ALTA: COMPRA***********
    public static void addLoteInfoToMovimientoAlta(final Lote lote, final Movimiento movimiento) {
        String timestampLoteDTO = lote.getFechaYHoraCreacion()
            .format(DateTimeFormatter.ofPattern("yy.MM.dd_HH.mm.ss"));
        movimiento.setCodigoMovimiento(lote.getCodigoLote() + "-" + timestampLoteDTO);
        movimiento.setLote(lote);
        for (Bulto bulto : lote.getBultos()) {
            populateDetalleMovimientoAlta(movimiento, bulto);
        }
    }

    //***********CU14 BAJA: RETIRO MERCADO***********
    public static Movimiento crearMovimientoBajaRecall(final MovimientoDTO dto) {
        Movimiento movimiento = new Movimiento();

        movimiento.setTipoMovimiento(TipoMovimientoEnum.BAJA);
        movimiento.setMotivo(RETIRO_MERCADO);

        movimiento.setFechaYHoraCreacion(dto.getFechaYHoraCreacion());
        String timestampLoteDTO = dto.getFechaYHoraCreacion()
            .format(DateTimeFormatter.ofPattern("yy.MM.dd_HH.mm.ss"));
        movimiento.setCodigoMovimiento(dto.getCodigoLote() + "-" + timestampLoteDTO);
        movimiento.setDictamenFinal(DictamenEnum.RETIRO_MERCADO);

        movimiento.setFecha(dto.getFechaMovimiento());

        movimiento.setActivo(true);
        movimiento.setObservaciones("_CU14_\n" + dto.getObservaciones());
        return movimiento;
    }

    //***********CU4 BAJA: DEVOLUCION COMPRA***********
    public static Movimiento crearMovimientoDevolucionCompra(final MovimientoDTO dto) {
        Movimiento movimiento = new Movimiento();

        movimiento.setTipoMovimiento(TipoMovimientoEnum.BAJA);
        movimiento.setMotivo(DEVOLUCION_COMPRA);

        movimiento.setFechaYHoraCreacion(dto.getFechaYHoraCreacion());
        String timestampLoteDTO = dto.getFechaYHoraCreacion()
            .format(DateTimeFormatter.ofPattern("yy.MM.dd_HH.mm.ss"));
        movimiento.setCodigoMovimiento(dto.getCodigoLote() + "-" + timestampLoteDTO);
        movimiento.setDictamenFinal(DictamenEnum.RECHAZADO);

        movimiento.setFecha(dto.getFechaMovimiento());

        movimiento.setActivo(true);
        movimiento.setObservaciones("_CU4_\n" + dto.getObservaciones());
        return movimiento;
    }

    //***********CU1 ALTA: COMPRA***********
    public static Movimiento createMovimientoAltaIngresoCompra(final Lote lote) {
        Movimiento movimiento = createMovimientoAlta(lote);
        movimiento.setObservaciones("_CU1_\n" + lote.getObservaciones());
        movimiento.setMotivo(MotivoEnum.COMPRA);
        return movimiento;
    }

    //***********CU10 ALTA: PRODUCCION INTERNA***********
    public static Movimiento createMovimientoAltaIngresoProduccion(final Lote lote) {
        Movimiento movimiento = createMovimientoAlta(lote);
        movimiento.setObservaciones("_CU10_\n" + lote.getObservaciones());
        movimiento.setMotivo(MotivoEnum.PRODUCCION_PROPIA);
        return movimiento;
    }

    //***********CU7 BAJA: CONSUMO PRODUCCION***********
    public static Movimiento createMovimientoBajaProduccion(final LoteDTO dto, final Lote lote) {
        Movimiento movimiento = new Movimiento();
        movimiento.setTipoMovimiento(TipoMovimientoEnum.BAJA);
        movimiento.setMotivo(MotivoEnum.CONSUMO_PRODUCCION);
        movimiento.setFechaYHoraCreacion(dto.getFechaYHoraCreacion());
        String timestampLoteDTO = dto.getFechaYHoraCreacion()
            .format(DateTimeFormatter.ofPattern("yy.MM.dd_HH.mm.ss"));
        movimiento.setCodigoMovimiento(lote.getCodigoLote() + "-" + timestampLoteDTO);
        movimiento.setFecha(dto.getFechaEgreso());
        movimiento.setObservaciones(dto.getObservaciones());
        movimiento.setObservaciones("_CU7_\n" + dto.getObservaciones());
        movimiento.setLote(lote);
        movimiento.setActivo(true);

        return movimiento;
    }

    //***********CU12 BAJA: VENTA***********
    public static Movimiento createMovimientoBajaVenta(final LoteDTO dto, final Lote lote) {
        Movimiento movimiento = new Movimiento();
        movimiento.setTipoMovimiento(TipoMovimientoEnum.BAJA);
        movimiento.setMotivo(MotivoEnum.VENTA);
        movimiento.setFechaYHoraCreacion(dto.getFechaYHoraCreacion());
        String timestampLoteDTO = dto.getFechaYHoraCreacion()
            .format(DateTimeFormatter.ofPattern("yy.MM.dd_HH.mm.ss"));
        movimiento.setCodigoMovimiento(lote.getCodigoLote() + "-" + timestampLoteDTO);
        movimiento.setFecha(dto.getFechaEgreso());
        movimiento.setObservaciones("_CU12_\n" + dto.getObservaciones());
        movimiento.setLote(lote);
        movimiento.setActivo(true);

        return movimiento;
    }

    //*********************PUBLIC COMMONS************************
    public static Movimiento createMovimientoModificacion(final MovimientoDTO dto, final Lote lote) {
        Movimiento movimiento = new Movimiento();
        movimiento.setTipoMovimiento(TipoMovimientoEnum.MODIFICACION);
        movimiento.setFechaYHoraCreacion(dto.getFechaYHoraCreacion());
        String timestampLoteDTO = dto.getFechaYHoraCreacion()
            .format(DateTimeFormatter.ofPattern("yy.MM.dd_HH.mm.ss"));
        movimiento.setCodigoMovimiento(lote.getCodigoLote() + "-" + timestampLoteDTO);

        movimiento.setObservaciones(dto.getObservaciones());
        movimiento.setLote(lote);
        movimiento.setActivo(true);

        return movimiento;
    }

    //***********CU3 BAJA: MUESTREO***********
    public static Movimiento createMovimientoMuestreoConAnalisis(
        final MovimientoDTO dto,
        final Bulto bulto,
        final Analisis ultimoAnalisis) {
        final Movimiento movimiento = createMovimientoPorMuestreo(dto);
        populateDetalleMovimiento(movimiento, bulto);
        String timestampLoteDTO = dto.getFechaYHoraCreacion()
            .format(DateTimeFormatter.ofPattern("yy.MM.dd_HH.mm.ss"));
        movimiento.setCodigoMovimiento(bulto.getLote().getCodigoLote() +
            "-B_" +
            bulto.getNroBulto() +
            "-" +
            timestampLoteDTO);
        movimiento.setLote(bulto.getLote());
        movimiento.setNroAnalisis(ultimoAnalisis.getNroAnalisis());
        return movimiento;
    }

    //*********************PRIVATE COMMONS************************
    //************************************************************

    //***********CU1 ALTA: COMPRA***********
    //***********CU10 ALTA: PRODUCCION INTERNA***********
    static Movimiento createMovimientoAlta(final Lote lote) {
        Movimiento movimiento = new Movimiento();
        movimiento.setTipoMovimiento(TipoMovimientoEnum.ALTA);
        movimiento.setFechaYHoraCreacion(lote.getFechaYHoraCreacion());
        String timestampLoteDTO = lote.getFechaYHoraCreacion()
            .format(DateTimeFormatter.ofPattern("yy.MM.dd_HH.mm.ss"));
        movimiento.setCodigoMovimiento(lote.getCodigoLote() + "-" + timestampLoteDTO);
        movimiento.setFecha(lote.getFechaYHoraCreacion().toLocalDate());
        movimiento.setCantidad(lote.getCantidadInicial());
        movimiento.setUnidadMedida(lote.getUnidadMedida());
        movimiento.setDictamenFinal(lote.getDictamen());
        movimiento.setLote(lote);
        movimiento.setActivo(true);
        return movimiento;
    }


    //***********CU3 BAJA: MUESTREO***********
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

        movimiento.setObservaciones("_CU3_\n" + dto.getObservaciones());
        return movimiento;
    }

    //***********CU3 BAJA: MUESTREO***********
    static void populateDetalleMovimiento(final Movimiento movimiento, final Bulto bulto) {
        DetalleMovimiento det = DetalleMovimiento.builder()
            .movimiento(movimiento)
            .bulto(bulto)
            .cantidad(movimiento.getCantidad())
            .unidadMedida(movimiento.getUnidadMedida())
            .build();
        movimiento.getDetalles().add(det);
        bulto.getDetalles().add(det);
    }

    //***********CU1 ALTA: COMPRA***********
    static void populateDetalleMovimientoAlta(final Movimiento movimiento, final Bulto bulto) {
        DetalleMovimiento det = DetalleMovimiento.builder()
            .movimiento(movimiento)
            .bulto(bulto)
            .cantidad(bulto.getCantidadInicial())
            .unidadMedida(bulto.getUnidadMedida())
            .build();
        movimiento.getDetalles().add(det);
        bulto.getDetalles().add(det);
    }

}
